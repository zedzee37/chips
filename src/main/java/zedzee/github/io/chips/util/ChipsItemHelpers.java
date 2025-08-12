package zedzee.github.io.chips.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.item.ItemStack;

import java.util.Map;

public class ChipsItemHelpers {
    public static boolean stackHasChips(ItemStack stack) {
        if (!stack.contains(DataComponentTypes.BLOCK_STATE)) {
            return false;
        }

        BlockStateComponent blockState = stack.get(DataComponentTypes.BLOCK_STATE);
        return blockState.properties().containsKey(ChipsBlockHelpers.CHIPS.getName());
    }

    public static int getChipsFromStack(ItemStack stack) {
        if (!stackHasChips(stack)) {
            return -1;
        }

        return stack.get(DataComponentTypes.BLOCK_STATE).getValue(ChipsBlockHelpers.CHIPS);
    }

    public static ItemStack getStackWithChipsCopy(ItemStack stack, int chips) {
        return getStackWithChips(stack.copy(), chips);
    }

    public static ItemStack getStackWithChips(ItemStack stack, int chips) {
        Map<String, String> propertyMap = Map.ofEntries(
                Map.entry(ChipsBlockHelpers.CHIPS.getName(), String.valueOf(chips))
        );
        stack.set(DataComponentTypes.BLOCK_STATE, new BlockStateComponent(propertyMap));
        return stack;
    }
}
