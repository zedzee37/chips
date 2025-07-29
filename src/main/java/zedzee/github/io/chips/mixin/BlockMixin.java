package zedzee.github.io.chips.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zedzee.github.io.chips.util.ShapeHelpers;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    public void injectChips(AbstractBlock.Settings settings, CallbackInfo ci) {
        Block block = (Block)(Object)this;
        if (block.getDefaultState().contains(ShapeHelpers.CHIPS_PROPERTY)) {
            block.setDefaultState(block.getDefaultState().with(ShapeHelpers.CHIPS_PROPERTY, 1));
        }
    }

    @Inject(method = "appendProperties", at = @At("HEAD"))
    public void addChips(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(ShapeHelpers.CHIPS_PROPERTY);
    }
}
