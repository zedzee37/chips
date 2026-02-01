package zedzee.github.io.chips.mixin;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.block.CornerInfo;
import zedzee.github.io.chips.block.entity.ChipsBlockEntity;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow
    protected ServerWorld world;

    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Inject(
            method = "tryBreakBlock",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z",
                    shift = At.Shift.BEFORE),
            cancellable = true)
    public void chipChipsBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState blockState = world.getBlockState(pos);
        if (blockState.isOf(ChipsBlocks.CHIPS_BLOCK)) {
            CornerInfo hoveredCorner = ChipsBlock.getHoveredCorner(world, player);

            if (hoveredCorner == null || !hoveredCorner.exists()) {
                cir.setReturnValue(false);
                return;
            }

            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (!(blockEntity instanceof ChipsBlockEntity chipsBlockEntity)) return;

            Block block = chipsBlockEntity.getBlockAtCorner(hoveredCorner);
            if (block == null) {
                cir.setReturnValue(false);
                return;
            }
            if (!player.getMainHandStack().getItem().canMine(block.getDefaultState(),
                    world,
                    pos,
                    player)) {
                cir.setReturnValue(false);
                cir.cancel();
            }

            boolean broken = false;
            if ((chipsBlockEntity.getTotalChips() & ~hoveredCorner.shape()) == 0) {
                blockState.getBlock().onBreak(world, pos, blockState, player);
                broken = true;
            }

            chipsBlockEntity.removeChips(hoveredCorner);

            if (broken) {
                blockState.getBlock().onBroken(world, pos, blockState);
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }

            cir.cancel();
        }
    }
}
