package josgoi.blackjack.registry;

import josgoi.blackjack.block.BlackjackTableBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final BlackjackTableBlock BLACKJACK_TABLE = register(
            "blackjack_table",
            new BlackjackTableBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.RED)
                    .strength(2.5f)
                    .nonOpaque())
    );

    private static <T extends Block> T register(String path, T block) {
        Identifier id = Identifier.of("blackjack", path);
        Registry.register(Registries.BLOCK, id, block);
        // Registra tambien el BlockItem para poder tenerlo en el inventario / crearlo.
        Registry.register(Registries.ITEM, id,
                new net.minecraft.item.BlockItem(block, new net.minecraft.item.Item.Settings()));
        return block;
    }

    /** Llamar desde el ModInitializer principal para forzar que la clase se cargue. */
    public static void register() {
    }
}
