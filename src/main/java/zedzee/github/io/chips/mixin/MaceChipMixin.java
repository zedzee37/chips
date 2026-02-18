package zedzee.github.io.chips.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.block.CornerInfo;
import zedzee.github.io.chips.block.entity.ChipsBlockEntity;

@Mixin(ItemStack.class)
public class MaceChipMixin {
    @Inject(method = "useOnBlock", at = @At("HEAD"))
    public void chipBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        final BlockPos pos = context.getBlockPos();
        final BlockState state = context.getWorld().getBlockState(pos);

        if (state.isOf(ChipsBlocks.CHIPS_BLOCK)) return;

        final World world = context.getWorld();
        world.setBlockState(pos, ChipsBlocks.CHIPS_BLOCK.getDefaultState());

        final BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof final ChipsBlockEntity chipsBlockEntity)) {
            return;
        }

        chipsBlockEntity.setChips(state, CornerInfo.fromShape(255));
        context.getPlayer().swingHand(context.getHand());
    }
}
