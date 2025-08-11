package zedzee.github.io.chips.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import zedzee.github.io.chips.block.ChipsBlockHelpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(AbstractBlock.class)
public class ModifyBlockDropsMixin {
    @ModifyReturnValue(method = "getDroppedStacks", at = @At("RETURN"))
    private List<ItemStack> addBlockStateComponent(
            List<ItemStack> original,
            BlockState state
    ) {
        if (!state.contains(ChipsBlockHelpers.CHIPS)) {
            return original;
        }

        int chips = state.get(ChipsBlockHelpers.CHIPS);
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put(ChipsBlockHelpers.CHIPS.getName(), String.valueOf(chips));

        original
                .stream()
                .filter(stack ->
                        stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock().equals(state.getBlock())
                )
                .forEach(stack ->
                    stack.set(DataComponentTypes.BLOCK_STATE, new BlockStateComponent(propertyMap))
                );
        return original;
    }
}
