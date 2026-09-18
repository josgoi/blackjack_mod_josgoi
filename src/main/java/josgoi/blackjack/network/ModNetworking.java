package josgoi.blackjack.network;

import josgoi.blackjack.block.BlackjackTableBlockEntity;
import josgoi.blackjack.network.BlackjackPayloads.BetPayload;
import josgoi.blackjack.network.BlackjackPayloads.GameStatePayload;
import josgoi.blackjack.network.BlackjackPayloads.HitPayload;
import josgoi.blackjack.network.BlackjackPayloads.StandPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;

/**
 * Llamar a registerCommon() UNA vez desde el ModInitializer principal
 * (funciona tanto en cliente como en servidor dedicado).
 */
public class ModNetworking {

    public static void registerCommon() {
        // Registrar los tipos de payload (hay que hacerlo en ambos canales)
        PayloadTypeRegistry.playC2S().register(BetPayload.ID, BetPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(HitPayload.ID, HitPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StandPayload.ID, StandPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GameStatePayload.ID, GameStatePayload.CODEC);

        // ---- Receptores del lado servidor ----
        // Nota: usamos context.player() para saber quien mando el paquete, y
        // buscamos que mesa (BlockEntity) tiene abierta ese jugador via su
        // ScreenHandler actual (ver BlackjackScreenHandler).

        ServerPlayNetworking.registerGlobalReceiver(BetPayload.ID, (payload, context) -> {
            context.player().server.execute(() -> {
                findOpenTable(context.player()).ifPresent(table -> table.onBet(context.player(), payload.amount()));
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(HitPayload.ID, (payload, context) -> {
            context.player().server.execute(() -> {
                findOpenTable(context.player()).ifPresent(table -> table.onHit(context.player()));
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(StandPayload.ID, (payload, context) -> {
            context.player().server.execute(() -> {
                findOpenTable(context.player()).ifPresent(table -> table.onStand(context.player()));
            });
        });
    }

    private static java.util.Optional<BlackjackTableBlockEntity> findOpenTable(net.minecraft.server.network.ServerPlayerEntity player) {
        if (player.currentScreenHandler instanceof josgoi.blackjack.screen.BlackjackScreenHandler handler) {
            BlockEntity be = handler.getBlockEntity();
            if (be instanceof BlackjackTableBlockEntity table) {
                return java.util.Optional.of(table);
            }
        }
        return java.util.Optional.empty();
    }
}
