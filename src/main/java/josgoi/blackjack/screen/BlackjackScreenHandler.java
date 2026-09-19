package josgoi.blackjack.screen;

import josgoi.blackjack.block.BlackjackTableBlockEntity;
import josgoi.blackjack.registry.ModScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
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

        // Estas ranuras no se ven (quedan fuera de la pantalla): las agregamos
        // solo para que el juego detecte cambios en el inventario del jugador
        // (como las emeraldas al apostar o cobrar) y se los mande al cliente
        // al instante, en vez de esperar a que cierres el GUI.
        int hiddenX = -1000;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, hiddenX + col * 18, row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, hiddenX + col * 18, 58));
        }
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
