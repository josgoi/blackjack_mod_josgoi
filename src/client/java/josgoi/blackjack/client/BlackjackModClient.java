package josgoi.blackjack.client;

import josgoi.blackjack.registry.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

/**
 * NOTA: igual que con BlackjackMod, si ya tenes un ClientModInitializer,
 * fusiona estas dos lineas dentro de tu onInitializeClient() existente.
 */
public class BlackjackModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.BLACKJACK_SCREEN_HANDLER, BlackjackScreen::new);
        BlackjackClientNetworking.register();
    }
}
