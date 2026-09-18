package josgoi.blackjack.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * El bloque fisico de la mesa. Al hacer click derecho, si el jugador es
 * el servidor, le abre el GUI (BlackjackScreenHandler) a traves de la
 * BlockEntity, que hace de NamedScreenHandlerFactory.
 *
 * Nota: extiende BlockWithEntity porque necesita guardar estado (la partida
 * en curso) en una BlockEntity.
 */
public class BlackjackTableBlock extends BlockWithEntity {

    public BlackjackTableBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
                                  net.minecraft.util.hit.BlockHitResult hit) {
        if (!world.isClient) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof BlackjackTableBlockEntity table) {
                player.openHandledScreen(table);
                if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                    table.sendStateTo(serverPlayer);
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected com.mojang.serialization.MapCodec<BlackjackTableBlock> getCodec() {
        // Requerido desde que los bloques con BlockEntity usan un "codec"
        // para poder guardarse/registrarse. No necesitamos nada especial,
        // solo delegar al constructor.
        return createCodec(settings -> new BlackjackTableBlock(settings));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BlackjackTableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, josgoi.blackjack.registry.ModBlockEntities.BLACKJACK_TABLE, BlackjackTableBlockEntity::tick);
    }
}
