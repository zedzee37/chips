package zedzee.github.io.chips.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zedzee.github.io.chips.block.ChipsBlockHelpers;

@Mixin(AbstractBlock.class)
public class OutlineShapeMixin {
    @Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
    public void setCorrectShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (state != null && world != null && context != null && state.contains(ChipsBlockHelpers.CHIPS)) {
            int shape = state.get(ChipsBlockHelpers.CHIPS);

            if (shape != 0 && shape != 255) {
                cir.setReturnValue(ChipsBlockHelpers.getOutlineShape(state));
            }
        }
    }
}
