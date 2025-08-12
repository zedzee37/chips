package zedzee.github.io.chips.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import zedzee.github.io.chips.util.ChipsBlockHelpers;

@Mixin(AbstractBlock.class)
public class OutlineShapeMixin {
    @ModifyReturnValue(method = "getOutlineShape", at = @At("RETURN"))
    public VoxelShape setCorrectShape(VoxelShape original, BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state != null && world != null && context != null && state.contains(ChipsBlockHelpers.CHIPS)) {
            int shape = state.get(ChipsBlockHelpers.CHIPS);

            if (shape != 0 && shape != 255) {
                return ChipsBlockHelpers.getOutlineShape(state);
            }
        }
        return original;
    }
}
