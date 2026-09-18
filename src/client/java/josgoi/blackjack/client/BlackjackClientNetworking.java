package josgoi.blackjack.client;

import josgoi.blackjack.network.BlackjackPayloads.GameStatePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

/** Llamar desde el ClientModInitializer. */
public class BlackjackClientNetworking {

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(GameStatePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (MinecraftClient.getInstance().currentScreen instanceof BlackjackScreen screen) {
                    screen.updateState(payload);
                }
            });
        });
    }
}
