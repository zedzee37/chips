package zedzee.github.io.chips.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import zedzee.github.io.chips.util.ChipsBlockHelpers;

@Mixin(BlockItem.class)
public class DefaultStackMixin {
    @ModifyReturnValue(method = "getPlacementState", at = @At("RETURN"))
    public BlockState placeWithChips(BlockState original, ItemPlacementContext context) {
        ItemStack stack = context.getStack();

        if (
                !(stack.getItem() instanceof BlockItem blockItem) ||
                        !blockItem.getBlock().getDefaultState().contains(ChipsBlockHelpers.CHIPS)
        ) {
            return original;
        }

        if (!stack.contains(DataComponentTypes.BLOCK_STATE)) {
            return original.with(ChipsBlockHelpers.CHIPS, 255);
        }

        return original;
    }

//    public ItemStack addChipsToStack(ItemStack stack) {
//    }
}
