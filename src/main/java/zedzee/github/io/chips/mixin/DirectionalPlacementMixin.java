package zedzee.github.io.chips.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import zedzee.github.io.chips.util.ChipsBlockHelpers;

@Mixin(Block.class)
public class DirectionalPlacementMixin {
    @ModifyReturnValue(method = "getPlacementState", at = @At("RETURN"))
    public BlockState placeDirectional(BlockState original, ItemPlacementContext ctx) {
        if (!original.contains(ChipsBlockHelpers.FACING)) {
            return original;
        }

        return original.with(ChipsBlockHelpers.FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }
}
