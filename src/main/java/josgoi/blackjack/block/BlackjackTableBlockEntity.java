package josgoi.blackjack.block;

import josgoi.blackjack.game.BlackjackGame;
import josgoi.blackjack.network.BlackjackPayloads.GameStatePayload;
import josgoi.blackjack.registry.ModBlockEntities;
import josgoi.blackjack.screen.BlackjackScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class BlackjackTableBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos> {

    private final BlackjackGame game = new BlackjackGame();
    private final CardDisplayManager cardDisplay = new CardDisplayManager();
    private boolean payoutApplied = false;
    private int actionTickTimer = 0;
    private ServerPlayerEntity currentPlayer; // el jugador de la ronda actual
    private boolean pendingHit = false; // true mientras esperamos para entregar la carta pedida

    public BlackjackTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLACKJACK_TABLE, pos, state);
    }

    // ----- Acciones que llegan desde ModNetworking -----

    public void onBet(ServerPlayerEntity player, int amount) {
        if (game.phase() == BlackjackGame.Phase.PLAYER_TURN
                || game.phase() == BlackjackGame.Phase.DEALER_TURN
                || game.phase() == BlackjackGame.Phase.DEALING) {
            player.sendMessage(Text.literal("Ya hay una ronda en curso."), false);
            return;
        }
        if (amount <= 0) return;

        int emeraldsHeld = countItem(player, Items.EMERALD);
        if (emeraldsHeld < amount) {
            player.sendMessage(Text.literal("No tienes suficientes emeraldas (tienes " + emeraldsHeld + ")."), false);
            return;
        }

        removeItems(player, Items.EMERALD, amount);
        payoutApplied = false;
        currentPlayer = player;
        game.startRound(amount); // deja el juego en fase DEALING, con las 4 cartas pendientes
        actionTickTimer = 10; // medio segundo antes de la primera carta
        onStateChanged();
    }

    public void onHit(ServerPlayerEntity player) {
        if (game.phase() != BlackjackGame.Phase.PLAYER_TURN || pendingHit) return;
        pendingHit = true;
        actionTickTimer = 10; // medio segundo antes de que aparezca la carta pedida
    }

    public void onStand(ServerPlayerEntity player) {
        if (game.phase() != BlackjackGame.Phase.PLAYER_TURN) return;
        game.stand();
        actionTickTimer = 20; // ~1 segundo antes de que el dealer pida su primera carta
        onStateChanged();
    }

    /** Se llama una vez por tick del servidor mientras el bloque esta cargado. */
    public static void tick(net.minecraft.world.World world, BlockPos pos, BlockState state, BlackjackTableBlockEntity be) {
        if (world.isClient) return;
        be.tickAuto((ServerWorld) world);
    }

    /** Avanza el reparto inicial paso a paso, o el turno del dealer paso a paso, segun la fase. */
    /** Avanza el reparto inicial paso a paso, o el turno del dealer paso a paso, segun la fase. */
    private void tickAuto(ServerWorld world) {
        if (pendingHit) {
            if (actionTickTimer > 0) {
                actionTickTimer--;
                return;
            }
            pendingHit = false;
            game.hit();
            settleIfRoundJustEnded(currentPlayer);
            onStateChanged();
            return;
        }

        if (game.phase() == BlackjackGame.Phase.DEALING) {
            if (actionTickTimer > 0) {
                actionTickTimer--;
                return;
            }
            if (game.dealStep()) {
                actionTickTimer = 10; // medio segundo entre carta y carta del reparto inicial
                onStateChanged();
            } else {
                game.finishDealing();
                if (game.phase() == BlackjackGame.Phase.DEALER_TURN) {
                    // Blackjack natural: el dealer ya tiene que empezar a jugar su turno.
                    actionTickTimer = 20;
                }
                onStateChanged();
            }
            return;
        }

        if (game.phase() == BlackjackGame.Phase.DEALER_TURN) {
            if (actionTickTimer > 0) {
                actionTickTimer--;
                return;
            }
            if (game.dealerHitStep()) {
                actionTickTimer = 20; // 1 segundo entre carta y carta
                onStateChanged();
            } else {
                game.finishDealerTurn();
                if (currentPlayer != null) {
                    settleIfRoundJustEnded(currentPlayer);
                }
                onStateChanged();
            }
        }
    }

    /** Si la ronda acaba de terminar, le paga (o no) al jugador y deja la mesa lista para apostar de nuevo. */
    private void settleIfRoundJustEnded(ServerPlayerEntity player) {
        if (game.phase() == BlackjackGame.Phase.ROUND_OVER && !payoutApplied) {
            int payout = game.payout();
            if (payout > 0) {
                giveItems(player, Items.EMERALD, payout);
            }
            player.sendMessage(Text.literal(resultMessage(game.lastResult(), payout)), false);
            payoutApplied = true;
        }
    }

    private String resultMessage(BlackjackGame.Result result, int payout) {
        return switch (result) {
            case PLAYER_BLACKJACK -> "Blackjack! Ganas " + payout + " esmeraldas.";
            case PLAYER_WIN -> "Tu ganas! +" + payout + " emeralds.";
            case DEALER_BUST -> "El dealer se paso! Ganas +" + payout + " esmeraldas.";
            case PUSH -> "Empate, se te devuelve la apuesta.";
            case DEALER_WIN -> "Gana la casa. Has perdido la apuesta.";
            case PLAYER_BUST -> "Te has pasado de 21. Has perdido la apuesta.";
            case NONE -> "";
        };
    }

    /** Manda el estado nuevo a todos los que tengan este GUI abierto y actualiza las cartas visuales. */
    private void onStateChanged() {
        if (world instanceof ServerWorld serverWorld) {
            cardDisplay.updateDisplay(serverWorld, pos, game.playerHand(), game.dealerHand());
        }
        GameStatePayload payload = GameStatePayload.from(game);
        for (PlayerEntity p : world.getPlayers()) {
            if (p instanceof ServerPlayerEntity serverPlayer
                    && serverPlayer.currentScreenHandler instanceof BlackjackScreenHandler handler
                    && handler.getBlockEntity() == this) {
                ServerPlayNetworking.send(serverPlayer, payload);
            }
        }
    }

    // ----- Helpers de inventario -----

    private static int countItem(PlayerEntity player, net.minecraft.item.Item item) {
        int total = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            var stack = player.getInventory().getStack(i);
            if (stack.isOf(item)) total += stack.getCount();
        }
        return total;
    }

    private static void removeItems(PlayerEntity player, net.minecraft.item.Item item, int amount) {
        PlayerInventory inv = player.getInventory();
        int remaining = amount;
        for (int i = 0; i < inv.size() && remaining > 0; i++) {
            var stack = inv.getStack(i);
            if (stack.isOf(item)) {
                int take = Math.min(remaining, stack.getCount());
                stack.decrement(take);
                remaining -= take;
            }
        }
    }

    private static void giveItems(ServerPlayerEntity player, net.minecraft.item.Item item, int amount) {
        var stack = new net.minecraft.item.ItemStack(item, amount);
        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
    }

    /** Manda el estado actual solo a un jugador (usado al abrir el GUI por primera vez). */
    public void sendStateTo(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, GameStatePayload.from(game));
    }

    // ----- ExtendedScreenHandlerFactory -----

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return pos; // se lo mandamos al cliente para que sepa que mesa es (por si hay varias)
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Mesa de Blackjack");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new BlackjackScreenHandler(syncId, inv, this);
    }

    public BlackjackGame game() {
        return game;
    }
}