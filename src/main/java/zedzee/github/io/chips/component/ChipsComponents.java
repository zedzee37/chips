package zedzee.github.io.chips.component;

import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import zedzee.github.io.chips.Chips;

public class ChipsComponents {
    public static final ComponentType<BlockComponent> BLOCK_COMPONENT_COMPONENT = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Chips.identifier("block_component"),
            ComponentType.<BlockComponent>builder().codec(BlockComponent.CODEC).build()
    );

    public static void init() {}
}
