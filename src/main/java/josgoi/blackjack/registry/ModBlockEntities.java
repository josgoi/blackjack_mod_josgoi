package josgoi.blackjack.registry;

import josgoi.blackjack.block.BlackjackTableBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    public static final BlockEntityType<BlackjackTableBlockEntity> BLACKJACK_TABLE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of("blackjack", "blackjack_table"),
            BlockEntityType.Builder.create(BlackjackTableBlockEntity::new, ModBlocks.BLACKJACK_TABLE).build()
    );

    public static void register() {
    }
}
