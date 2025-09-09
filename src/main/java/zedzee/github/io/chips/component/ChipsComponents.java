package zedzee.github.io.chips.component;

import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Unit;
import zedzee.github.io.chips.Chips;

public class ChipsComponents {
    public static final ComponentType<ChipsBlockItemComponent> BLOCK_COMPONENT_COMPONENT = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Chips.identifier("block_component"),
            ComponentType.<ChipsBlockItemComponent>builder().codec(ChipsBlockItemComponent.CODEC).build()
    );

    public static final ComponentType<Unit> INDIVIDUAL_CHIPS_COMPONENT_COMPONENT = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Chips.identifier("individual_chips"),
            ComponentType.<Unit>builder().codec(Unit.CODEC).build()
    );

    public static void init() {}
}
