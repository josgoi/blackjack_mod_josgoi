package josgoi.blackjack;

import josgoi.blackjack.network.ModNetworking;
import josgoi.blackjack.registry.ModBlockEntities;
import josgoi.blackjack.registry.ModBlocks;
import josgoi.blackjack.registry.ModScreenHandlers;
import josgoi.blackjack.registry.ModItems;
import net.fabricmc.api.ModInitializer;

/**
 * NOTA: si ya tenes un mod inicial creado (el template de fabric example mod),
 * no dupliques esta clase - agrega estas 4 lineas de "register()" dentro de
 * tu onInitialize() existente en vez de tener dos ModInitializer.
 */
public class BlackjackMod implements ModInitializer {

    public static final String MOD_ID = "blackjack";

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModBlockEntities.register();
        ModScreenHandlers.register();
        ModNetworking.registerCommon();
        ModItems.register();
    }
}
