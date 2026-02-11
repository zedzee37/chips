package zedzee.github.io.chips.client.mixin.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.block.CornerInfo;
import zedzee.github.io.chips.block.entity.ChipsBlockEntity;
import zedzee.github.io.chips.client.util.ChipsSpriteProvider;

@Mixin(BlockDustParticle.class)
public class ClientBlockDustParticleMixin {
    @Inject(
            method = "<init>(Lnet/minecraft/client/world/ClientWorld;DDDDDDLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/BlockDustParticle;setSprite(Lnet/minecraft/client/texture/Sprite;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void setCorrectChipsParticle(ClientWorld world,
                                         double x,
                                         double y,
                                         double z,
                                         double velocityX,
                                         double velocityY,
                                         double velocityZ,
                                         BlockState state,
                                         BlockPos blockPos,
                                         CallbackInfo ci) {
        if (state.isOf(ChipsBlocks.CHIPS_BLOCK)) {
            final MinecraftClient client = MinecraftClient.getInstance();
            final CornerInfo nearestCorner = ChipsBlock.getClosestSlice(world, blockPos, new Vec3d(x, y, z));
            if (nearestCorner == null || !nearestCorner.exists()) return;

            final ChipsBlockEntity blockEntity = (ChipsBlockEntity) world.getBlockEntity(blockPos);
            assert blockEntity != null;
            final BlockState blockState = blockEntity.getStateAtCorner(nearestCorner);
            if (blockState == null) return;

            BlockDustParticle blockDustParticle = (BlockDustParticle) (Object) this;
            Sprite sprite = client.getBlockRenderManager().getModel(
                    blockState
            ).getParticleSprite();
            blockDustParticle.setSprite(new ChipsSpriteProvider(sprite));
        }
    }
}
