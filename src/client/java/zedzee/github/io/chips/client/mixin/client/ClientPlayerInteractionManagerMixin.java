package zedzee.github.io.chips.client.mixin.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.block.CornerInfo;
import zedzee.github.io.chips.block.entity.ChipsBlockEntity;
import zedzee.github.io.chips.client.util.ChipsBlockBreakingProgress;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin implements ChipsBlockBreakingProgress {
    @Unique
    @Nullable
    private CornerInfo cornerInfo = null;

    @Shadow
    private float currentBreakingProgress;

    @Shadow
    public abstract void cancelBlockBreaking();

    @Shadow
    private boolean breakingBlock;

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    public abstract boolean attackBlock(BlockPos pos, Direction direction);

    @Shadow
    @Final
    private ClientPlayNetworkHandler networkHandler;

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"), cancellable = true)
    public void cancelBlockBreakingOnChipChange(BlockPos pos,
                           Direction direction,
                           CallbackInfoReturnable<Boolean> cir) {
        if (breakingBlock) {
            BlockState blockState = client.world.getBlockState(pos);
            if (blockState.isOf(ChipsBlocks.CHIPS_BLOCK) && cornerInfo != null) {
                CornerInfo current = ChipsBlock.getHoveredCorner(client.world, client.player);
                if (current.shape() != cornerInfo.shape()) {
                    cornerInfo = null;
                    currentBreakingProgress = 0.0f;
                    cancelBlockBreaking();
                    attackBlock(pos, direction);
                    cir.setReturnValue(false);
                    cir.cancel();
                }
            }
        }
    }

    @Inject(method = "breakBlock", at = @At("HEAD"))
    public void clearCorner(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        cornerInfo = null;
    }

    @Inject(method = "isCurrentlyBreaking", at = @At("RETURN"), cancellable = true)
    public void fixIsCurrentlyBreaking(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        boolean ret = cir.getReturnValue();
        cir.setReturnValue(ret && breakingBlock);
        cir.cancel();
    }

    @Override
    public CornerInfo chips$getCorner() {
        return cornerInfo;
    }

    @Override
    public void chips$setCorner(@Nullable CornerInfo cornerInfo) {
        this.cornerInfo = cornerInfo;
    }

    @Inject(
            method = "breakBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/block/BlockState;",
                    shift = At.Shift.BEFORE),
            cancellable = true
    )
    public void chipChipsBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState blockState = client.world.getBlockState(pos);
        if (blockState.isOf(ChipsBlocks.CHIPS_BLOCK)) {
            CornerInfo hoveredCorner = ChipsBlock.getHoveredCorner(client.world, client.player);
            BlockEntity blockEntity = client.world.getBlockEntity(pos);
            if (!(blockEntity instanceof ChipsBlockEntity chipsBlockEntity)) return;

            Block block = chipsBlockEntity.getBlockAtCorner(hoveredCorner);
            if (!this.client.player.getMainHandStack().getItem().canMine(block.getDefaultState(),
                    client.world,
                    pos,
                    this.client.player)) {
                cir.setReturnValue(false);
            }

            boolean broken = false;
            if ((chipsBlockEntity.getTotalChips() & ~hoveredCorner.shape()) == 0) {
                blockState.getBlock().onBreak(client.world, pos, blockState, client.player);
                broken = true;
            }

            if (broken) blockState.getBlock().onBroken(client.world, pos, blockState);

            cir.setReturnValue(false);
        }
    }
}
