package zedzee.github.io.chips.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.entity.ChipsBlockEntity;

import java.util.function.Function;

public class ChipsBlocks {
//    public static final Block CHIPS_BLOCK = register(
//            "chips",
//            ChipsBlock::new,
//            Block.Settings.create().nonOpaque()
//    );

//    public static final Block CHISELING_STATION = registerWithItem(
//            "chiseling_station",
//            ChiselingStation::new,
//            Block.Settings.create().mapColor(MapColor.OAK_TAN).instrument(NoteBlockInstrument.BASS).strength(2.5F).sounds(BlockSoundGroup.WOOD).burnable()
//    );

    public static final Block CHIPS_BLOCK = register("chips",
            ChipsBlock::new,
            AbstractBlock.Settings.create()
                    .nonOpaque()
                    .strength(0.5f)
                    .luminance(state -> state.get(ChipsBlock.LIGHT_LEVEL))
    );

    private static Block registerWithItem(String path,
                                          Function<AbstractBlock.Settings, Block> factory,
                                          AbstractBlock.Settings settings) {
        Block block = register(path, factory, settings);
        Items.register(block);
        return block;
    }

    private static Block register(String path,
                                          Function<AbstractBlock.Settings, Block> factory,
                                          AbstractBlock.Settings settings) {
        final Identifier identifier = Chips.identifier(path);
        final RegistryKey<Block> registryKey = RegistryKey.of(RegistryKeys.BLOCK, identifier);
//
//        settings.registryKey(registryKey);

        return Blocks.register(registryKey, factory.apply(settings));
    }

    public static void init() {

    }
}
