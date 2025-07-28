package zedzee.github.io.chips.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zedzee.github.io.chips.util.ShapeHelpers;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "appendProperties", at = @At("HEAD"))
    public void addChips(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(ShapeHelpers.CHIPS_PROPERTY);
    }

    @Inject(method = "getDefaultState", at = @At("TAIL"), cancellable = true)
    public void setMaxChips(CallbackInfoReturnable<BlockState> cir) {
        BlockState ret = cir.getReturnValue();
        if (ret.contains(ShapeHelpers.CHIPS_PROPERTY)) {
            cir.setReturnValue(ret.with(ShapeHelpers.CHIPS_PROPERTY, 255));
        }
    }
}
