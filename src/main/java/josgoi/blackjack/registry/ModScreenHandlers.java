package josgoi.blackjack.registry;

import josgoi.blackjack.screen.BlackjackScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {

    public static final ExtendedScreenHandlerType<BlackjackScreenHandler, net.minecraft.util.math.BlockPos> BLACKJACK_SCREEN_HANDLER =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    Identifier.of("blackjack", "blackjack_table"),
                    new ExtendedScreenHandlerType<>(
                            BlackjackScreenHandler::new,
                            net.minecraft.util.math.BlockPos.PACKET_CODEC
                    )
            );

    public static void register() {
    }
}
