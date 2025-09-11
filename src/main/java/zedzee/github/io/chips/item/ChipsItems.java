package zedzee.github.io.chips.item;

import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Unit;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.component.ChipsBlockItemComponent;
import zedzee.github.io.chips.component.ChipsComponents;

import java.util.function.Function;

public class ChipsItems {
    public static final int DEFAULT_USE_TIME = 50;

    public static final Item IRON_CHISEL = register(
            "iron_chisel",
            (settings) -> new ChiselItem(settings, DEFAULT_USE_TIME),
            createChiselSettings(
                    ToolMaterial.IRON
            )
    );
    public static final Item DIAMOND_CHISEL = register(
            "diamond_chisel",
            (settings) -> new ChiselItem(settings, DEFAULT_USE_TIME),
            createChiselSettings(
                    ToolMaterial.DIAMOND
            )
    );
    public static final Item NETHERITE_CHISEl = register(
            "netherite_chisel",
            (settings) -> new ChiselItem(settings, DEFAULT_USE_TIME),
            createChiselSettings(
                    ToolMaterial.NETHERITE
            )
    );

    public static final Item CREATIVE_CHISEL_ITEM = register(
            "creative_chisel",
            (settings) -> new ChiselItem(settings, ChiselItem.MIN_USE_TIME),
            createChiselSettings(
                    ToolMaterial.NETHERITE
            ).component(
                    DataComponentTypes.UNBREAKABLE,
                    Unit.INSTANCE
            )
    );

    public static final Item CHIPS_BLOCK_ITEM = register(
            "chips_block",
            ChipsBlockItem::new,
            new Item.Settings()
                    .component(
                            ChipsComponents.BLOCK_COMPONENT_COMPONENT,
                            new ChipsBlockItemComponent(Blocks.GRASS_BLOCK))
                    .component(
                            ChipsComponents.INDIVIDUAL_CHIPS_COMPONENT_COMPONENT,
                            Unit.INSTANCE)
    );

    public static final Item TEST_BLOCK_ITEM = register(
            "test_block",
            ChipsBlockItem::new,
            new Item.Settings()
                    .component(
                            ChipsComponents.BLOCK_COMPONENT_COMPONENT,
                            new ChipsBlockItemComponent(Blocks.COPPER_GRATE))
                    .component(
                            ChipsComponents.INDIVIDUAL_CHIPS_COMPONENT_COMPONENT,
                            Unit.INSTANCE)
    );

    private static Item.Settings createChiselSettings(ToolMaterial material) {
        return
                new Item.Settings()
                        .tool(material, BlockTags.AIR, 0.25f, 1.0f, 0.0f)
                        .maxCount(1)
                        .component(
                                ChipsComponents.INDIVIDUAL_CHIPS_COMPONENT_COMPONENT,
                                Unit.INSTANCE
                        );
    }

    private static Item register(String path, Function<Item.Settings, Item> factory, Item.Settings settings) {
        final RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, Chips.identifier(path));
        return Items.register(registryKey, factory, settings);
    }

    public static void init() {}
}

