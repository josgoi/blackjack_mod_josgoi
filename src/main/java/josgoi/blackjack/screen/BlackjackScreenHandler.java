package josgoi.blackjack.screen;

import josgoi.blackjack.block.BlackjackTableBlockEntity;
import josgoi.blackjack.registry.ModScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * No usamos slots de inventario tradicionales: todo el "contenido" (cartas,
 * apuesta, fase) se sincroniza aparte via BlackjackPayloads.GameStatePayload.
 * Este handler solo sirve para que el juego sepa "este jugador tiene abierta
 * esta mesa" (ver BlackjackTableBlockEntity#onStateChanged y ModNetworking).
 */
public class BlackjackScreenHandler extends ScreenHandler {

    private final BlackjackTableBlockEntity blockEntity;

    /** Constructor server-side: se crea directo desde BlackjackTableBlockEntity#createMenu. */
    public BlackjackScreenHandler(int syncId, PlayerInventory playerInventory, BlackjackTableBlockEntity blockEntity) {
        super(ModScreenHandlers.BLACKJACK_SCREEN_HANDLER, syncId);
        this.blockEntity = blockEntity;
    }

    /**
     * Constructor client-side: lo llama el registro de ModScreenHandlers usando
     * el BlockPos que mando ExtendedScreenHandlerFactory#getScreenOpeningData.
     */
    public BlackjackScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, resolveBlockEntity(playerInventory.player, pos));
    }

    private static BlackjackTableBlockEntity resolveBlockEntity(PlayerEntity player, BlockPos pos) {
        World world = player.getWorld();
        if (world.getBlockEntity(pos) instanceof BlackjackTableBlockEntity be) {
            return be;
        }
        throw new IllegalStateException("No hay BlackjackTableBlockEntity en " + pos);
    }

    public BlackjackTableBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true; // sin slots que validar; si queres, chequea distancia a blockEntity.getPos()
    }

    @Override
    public net.minecraft.item.ItemStack quickMove(PlayerEntity player, int slot) {
        return net.minecraft.item.ItemStack.EMPTY; // no hay slots
    }
}
