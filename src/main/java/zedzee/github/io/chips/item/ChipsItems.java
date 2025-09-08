package zedzee.github.io.chips.item;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.component.ChipsBlockItemComponent;
import zedzee.github.io.chips.component.ChipsComponents;
import zedzee.github.io.chips.component.IndividualChipsComponent;

import java.util.function.Function;

public class ChipsItems {
    public static final int DEFAULT_USE_TIME = 50;

    public static final Item CHISEL_ITEM = register(
            "chisel",
            (settings) -> new ChiselItem(settings, DEFAULT_USE_TIME),
            new Item.Settings()
                    .sword(ToolMaterial.IRON, 1.0f, 2.0f)
                    .maxDamage(100)
                    .maxCount(1)
                    .component(
                            ChipsComponents.INDIVIDUAL_CHIPS_COMPONENT_COMPONENT,
                            new IndividualChipsComponent())
    );
    public static final Item CREATIVE_CHISEL_ITEM = register(
            "creative_chisel",
            (settings) -> new ChiselItem(settings, 20),
            new Item.Settings()
                    .sword(ToolMaterial.NETHERITE, 1.0f, 2.0f)
                    .maxCount(1)
                    .component(
                            ChipsComponents.INDIVIDUAL_CHIPS_COMPONENT_COMPONENT,
                            new IndividualChipsComponent())
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
                            new IndividualChipsComponent())
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
                            new IndividualChipsComponent())
    );

    private static Item register(String path, Function<Item.Settings, Item> factory, Item.Settings settings) {
        final RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, Chips.identifier(path));
        return Items.register(registryKey, factory, settings);
    }

    public static void init() {}
}

