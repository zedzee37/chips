package zedzee.github.io.chips.mixin;

import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zedzee.github.io.chips.util.ShapeHelpers;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    @Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
    private void makeSmaller(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (state.contains(ShapeHelpers.CHIPS_PROPERTY)) {
            cir.setReturnValue(ShapeHelpers.getOutlineShape(state));
        }
    }
}
