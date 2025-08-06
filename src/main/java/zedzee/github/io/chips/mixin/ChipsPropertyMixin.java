package zedzee.github.io.chips.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zedzee.github.io.chips.block.ChipsBlockHelpers;

@Mixin(Block.class)
public class ChipsPropertyMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    public void setDefaultState(AbstractBlock.Settings settings, CallbackInfo ci) {
        Block block = (Block) (Object) this;

        BlockState defaultState = block.getDefaultState();
        if (defaultState.contains(ChipsBlockHelpers.CHIPS)) {
            block.setDefaultState(defaultState.with(ChipsBlockHelpers.CHIPS, 255));
        }
    }

    // fun hack to add this in !
    @Inject(method = "appendProperties", at = @At("HEAD"))
    public void injectChips(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        Block block = (Block) (Object) this;

        try {
            VoxelShape shape = block.getOutlineShape(block.getDefaultState(), null, BlockPos.ORIGIN, ShapeContext.absent());
            if (shape.equals(VoxelShapes.fullCube())) {
                builder.add(ChipsBlockHelpers.CHIPS);
            }
        } catch (Exception e) {
        }
    }
}
