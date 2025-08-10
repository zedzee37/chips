package zedzee.github.io.chips.item;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import zedzee.github.io.chips.Chips;

import java.util.function.Function;

public class ChipsItems {
    public static final int DEFAULT_USE_TIME = 200;

    public static final Item CHISEL_ITEM = register(
            "chisel",
            (settings) -> new ChiselItem(settings, DEFAULT_USE_TIME),
            new Item.Settings()
                    .maxDamage(100)
                    .maxCount(1)
    );
    public static final Item CREATIVE_CHISEL_ITEM = register(
            "creative_chisel",
            (settings) -> new ChiselItem(settings, 1),
            new Item.Settings()
                    .maxCount(1)
    );

    private static Item register(String path, Function<Item.Settings, Item> factory, Item.Settings settings) {
        final RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, Chips.identifier(path));
        return Items.register(registryKey, factory, settings);
    }

    public static void init() {}
}

