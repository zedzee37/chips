package zedzee.github.io.chips.client.mixin.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
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
import zedzee.github.io.chips.client.ChipsClient;
import zedzee.github.io.chips.client.util.ChipsBlockBreakingProgress;
import zedzee.github.io.chips.networking.BlockChippedPayload;
import zedzee.github.io.chips.networking.BlockSplitPayload;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin implements ChipsBlockBreakingProgress {
    @Unique
    private static final int CHIPPED_PARTICLE_COUNT = 10;
    @Unique
    private static final double CHIPPED_PARTICLE_VELOCITY_MIN = 3.0;
    @Unique
    private static final double CHIPPED_PARTICLE_VELOCITY_MAX = 5.0;

    @Unique
    private static final int SPLIT_PARTICLES_MIN = 10;
    @Unique
    private static final int SPLIT_PARTICLES_MAX = 20;
    @Unique
    private static final float SPLIT_PARTICLES_VELOCITY_MAX = 2;
    @Unique
    private static final float SPLIT_PARTICLES_VELOCITY_MIN = 1;

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
    public abstract boolean hasRidingInventory();

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    public void maceBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        assert client.world != null;
        BlockState state = client.world.getBlockState(pos);

        VoxelShape shape = state.getOutlineShape(client.world, pos);
        if (!shape.equals(VoxelShapes.fullCube())) {
            return;
        }

        assert client.player != null;
        if (!client.player.getMainHandStack().isOf(Items.MACE)) {
            return;
        }

        if (!ChipsClient.splittingOffCooldown()) {
            cir.setReturnValue(false);
            return;
        }

        if (state.isOf(ChipsBlocks.CHIPS_BLOCK)) {
            return;
        }

        client.world.setBlockState(pos, ChipsBlocks.CHIPS_BLOCK.getDefaultState());

        final BlockEntity blockEntity = client.world.getBlockEntity(pos);
        if (!(blockEntity instanceof final ChipsBlockEntity chipsBlockEntity)) {
            return;
        }

        addSplitParticles(client.world, client.world.getRandom(), state, pos, direction);

        chipsBlockEntity.setChips(state, CornerInfo.fromShape(255), false);
        ClientPlayNetworking.send(new BlockSplitPayload(pos));

        client.player.swingHand(Hand.MAIN_HAND);
        client.player.resetLastAttackedTicks();
        cir.setReturnValue(true);
    }

    @Unique
    private void addSplitParticles(ClientWorld world, Random random, BlockState state, BlockPos pos, Direction face) {
        Vec3d startPos = Vec3d.of(pos);
        Vec3d endPos = Vec3d.of(pos);

        switch (face) {
            case NORTH:
                endPos = endPos.add(1.0, 1.0, 0.0);
                break;
            case SOUTH:
                startPos = startPos.add(0.0, 0.0, 1.0);
                endPos = endPos.add(1.0, 1.0, 1.0);
                break;
            case WEST:
                endPos = endPos.add(0.0, 1.0, 1.0);
                break;
            case EAST:
                startPos = startPos.add(1.0, 0.0, 0.0);
                endPos = endPos.add(1.0, 1.0, 1.0);
                break;
            case DOWN:
                endPos = endPos.add(1.0, 0.0, 1.0);
                break;
            case UP:
                endPos = endPos.add(0.0, 1.0, 0.0);
                endPos = endPos.add(1.0, 1.0, 1.0);
        }

        int particleCount = random.nextBetween(SPLIT_PARTICLES_MIN, SPLIT_PARTICLES_MAX);
        for (int i = 0; i < particleCount; i++) {
            float randomX = random.nextFloat();
            float randomY = random.nextFloat();
            float randomZ = random.nextFloat();

            float randomVelocity = random.nextFloat() * SPLIT_PARTICLES_VELOCITY_MAX + SPLIT_PARTICLES_VELOCITY_MIN;
            Vec3d dir = Vec3d.of(face.getVector()).multiply(randomVelocity);

            BlockDustParticle particle = new BlockDustParticle(
                    world,
                    MathHelper.lerp(randomX, startPos.x, endPos.x),
                    MathHelper.lerp(randomY, startPos.y, endPos.y),
                    MathHelper.lerp(randomZ, startPos.z, endPos.z),
                    dir.getX(),
                    dir.getY(),
                    dir.getZ(),
                    state
            );

            client.particleManager.addParticle(particle);
        }
    }

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
        if (blockState != null && blockState.isOf(ChipsBlocks.CHIPS_BLOCK)) {
            CornerInfo hoveredCorner = ChipsBlock.getHoveredCorner(client.world, client.player);
            BlockEntity blockEntity = client.world.getBlockEntity(pos);
            if (!(blockEntity instanceof ChipsBlockEntity chipsBlockEntity)) return;

            BlockState state = chipsBlockEntity.getStateAtCorner(hoveredCorner);
            if (!this.client.player.getMainHandStack().getItem().canMine(state,
                    client.world,
                    pos,
                    this.client.player)) {
                cir.setReturnValue(false);
                cir.cancel();
            }

            boolean broken = false;
            if (chipsBlockEntity.getTotalChips().removeShape(hoveredCorner).isEmpty()) {
                blockState.getBlock().onBreak(client.world, pos, blockState, client.player);
                broken = true;
            }

            createChipsParticles(pos, hoveredCorner);
            chipsBlockEntity.removeChips(hoveredCorner, false);
            client.world.updateListeners(pos, blockState, blockState, Block.NOTIFY_ALL_AND_REDRAW);
            boolean shouldDrop = !client.player.isCreative();
            ClientPlayNetworking.send(new BlockChippedPayload(pos, hoveredCorner, shouldDrop));

            if (broken) {
                blockState.getBlock().onBroken(client.world, pos, blockState);
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }

            cir.cancel();
        }
    }

    @Unique
    private void createChipsParticles(BlockPos pos, CornerInfo cornerInfo) {
        final Random random = client.player.getRandom();
        final BlockState blockState = client.world.getBlockState(pos);

        final VoxelShape cornerShape = ChipsBlock.getShape(cornerInfo.shape());
        Vec3d midPoint = new Vec3d(
                cornerShape.getMin(Direction.Axis.X) + cornerShape.getMax(Direction.Axis.X),
                cornerShape.getMin(Direction.Axis.Y) + cornerShape.getMax(Direction.Axis.Y),
                cornerShape.getMin(Direction.Axis.Z) + cornerShape.getMax(Direction.Axis.Z)
        ).multiply(0.5);
        midPoint = midPoint.add(Vec3d.of(pos));

        for (int i = 0; i < CHIPPED_PARTICLE_COUNT; i++) {
            // why tf is this degrees and not radians, mojang fix please
            final Vec3d direction = Vec3d.fromPolar(
                    random.nextFloat() * 360,
                    random.nextFloat() * 360
            );
            final double velocityMagnitude = (random.nextDouble() * CHIPPED_PARTICLE_VELOCITY_MAX) + CHIPPED_PARTICLE_VELOCITY_MIN;

            final Vec3d velocity = direction.multiply(velocityMagnitude);

            BlockDustParticle blockDustParticle = new BlockDustParticle(
                    client.world,
                    midPoint.getX(),
                    midPoint.getY(),
                    midPoint.getZ(),
                    velocity.getX(),
                    velocity.getY(),
                    velocity.getZ(),
                    blockState
            );
            client.particleManager.addParticle(blockDustParticle);
        }
    }
}
