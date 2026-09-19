package josgoi.blackjack.client;

import josgoi.blackjack.network.BlackjackPayloads.BetPayload;
import josgoi.blackjack.network.BlackjackPayloads.GameStatePayload;
import josgoi.blackjack.network.BlackjackPayloads.HitPayload;
import josgoi.blackjack.network.BlackjackPayloads.StandPayload;
import josgoi.blackjack.registry.ModItems;
import josgoi.blackjack.screen.BlackjackScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

/**
 * GUI: muestra la mano del jugador y del dealer con los iconos reales de
 * cada carta (definidos en ModItems), no como texto.
 */
public class BlackjackScreen extends HandledScreen<BlackjackScreenHandler> {

    private GameStatePayload lastState = null;
    private int pendingBet = 10; // apuesta por defecto, ajustable con +/-

    private ButtonWidget hitButton;
    private ButtonWidget standButton;
    private ButtonWidget betButton;
    private ButtonWidget betUpButton;
    private ButtonWidget betDownButton;

    public BlackjackScreen(BlackjackScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 210;
        this.backgroundHeight = 160;
    }

    @Override
    protected void init() {
        super.init();
        int left = (width - backgroundWidth) / 2;
        int top = (height - backgroundHeight) / 2;

        hitButton = ButtonWidget.builder(Text.literal("Pedir"), b -> sendHit())
                .dimensions(left + 10, top + 120, 60, 20).build();
        standButton = ButtonWidget.builder(Text.literal("Plantarse"), b -> sendStand())
                .dimensions(left + 75, top + 120, 60, 20).build();
        betButton = ButtonWidget.builder(Text.literal("Repartir"), b -> sendBet())
                .dimensions(left + 145, top + 120, 55, 20).build();
        betDownButton = ButtonWidget.builder(Text.literal("-"), b -> adjustBet(-5))
                .dimensions(left + 10, top + 95, 20, 20).build();
        betUpButton = ButtonWidget.builder(Text.literal("+"), b -> adjustBet(5))
                .dimensions(left + 34, top + 95, 20, 20).build();

        addDrawableChild(hitButton);
        addDrawableChild(standButton);
        addDrawableChild(betButton);
        addDrawableChild(betDownButton);
        addDrawableChild(betUpButton);
    }

    private void adjustBet(int delta) {
        pendingBet = Math.max(5, pendingBet + delta);
    }

    private void sendBet() {
        ClientPlayNetworking.send(new BetPayload(pendingBet));
    }

    private void sendHit() {
        ClientPlayNetworking.send(new HitPayload());
    }

    private void sendStand() {
        ClientPlayNetworking.send(new StandPayload());
    }

    /** Llamado por BlackjackClientNetworking cuando llega un GameStatePayload nuevo. */
    public void updateState(GameStatePayload state) {
        this.lastState = state;
        boolean isPlayerTurn = "PLAYER_TURN".equals(state.phase());
        boolean canBet = "WAITING_FOR_BET".equals(state.phase()) || "ROUND_OVER".equals(state.phase());
        hitButton.active = isPlayerTurn;
        standButton.active = isPlayerTurn;
        betButton.active = canBet;
        betUpButton.active = canBet;
        betDownButton.active = canBet;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int left = (width - backgroundWidth) / 2;
        int top = (height - backgroundHeight) / 2;
        context.fill(left, top, left + backgroundWidth, top + backgroundHeight, 0xCC202020);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        if (lastState == null) {
            context.drawText(textRenderer, Text.literal("Esperando estado de la mesa..."), 10, 10, 0xFFFFFF, false);
            return;
        }

        context.drawText(textRenderer,
                Text.literal("Dealer (" + lastState.dealerValue() + ")"),
                10, 8, 0xFFFFFF, false);
        drawCardRow(context, lastState.dealerCards(), 10, 20);

        context.drawText(textRenderer,
                Text.literal("Tu mano (" + lastState.playerValue() + ")"),
                10, 55, 0xFFFFFF, false);
        drawCardRow(context, lastState.playerCards(), 10, 67);

        boolean roundInProgress = "DEALING".equals(lastState.phase())
                || "PLAYER_TURN".equals(lastState.phase())
                || "DEALER_TURN".equals(lastState.phase());
        int betToShow = roundInProgress ? lastState.currentBet() : pendingBet;
        context.drawText(textRenderer,
                Text.literal("Apuesta: " + betToShow + " esmeraldas"),
                60, 100, 0xFFD700, false);

        if (!"NONE".equals(lastState.lastResult()) && "ROUND_OVER".equals(lastState.phase())) {
            context.drawText(textRenderer, Text.literal("Resultado: " + lastState.lastResult()), 10, 145, 0x55FF55, false);
        }
    }

    /** Dibuja los iconos de una mano de cartas en fila, uno al lado del otro. */
    private void drawCardRow(DrawContext context, List<String> cardTextureIds, int x, int y) {
        int i = 0;
        for (String textureId : cardTextureIds) {
            Item item = ModItems.getCardItem(textureId);
            if (item != null) {
                context.drawItem(new ItemStack(item), x + i * 18, y);
            }
            i++;
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}