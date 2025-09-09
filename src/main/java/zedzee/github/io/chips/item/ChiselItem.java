package zedzee.github.io.chips.item;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.BrushItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.block.CornerInfo;
import zedzee.github.io.chips.block.entity.ChipsBlockEntity;
import zedzee.github.io.chips.networking.ChipsBlockChangePayload;
import zedzee.github.io.chips.networking.ChiselAnimationPayload;

import java.util.List;

public class ChiselItem extends Item {
    private static final int ANIMATION_TIME = 16;
    private final int useTime;

    public ChiselItem(Settings settings, int useTime) {
        super(settings);
        this.useTime = useTime;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity playerEntity = context.getPlayer();
        if (playerEntity != null) {
            HitResult result = this.getHitResult(playerEntity);

            if (result instanceof BlockHitResult blockHitResult) {
                World world = context.getWorld();
                BlockPos pos = blockHitResult.getBlockPos();

                boolean isChipsBlock = world.getBlockState(pos).isOf(ChipsBlocks.CHIPS_BLOCK);
                BlockEntity blockEntity = world.getBlockEntity(pos);

                if (playerEntity.isSneaking() && isChipsBlock && blockEntity instanceof ChipsBlockEntity chipsBlockEntity) {
                    int hitCorner = ChipsBlock.getHoveredCorner(world, context.getPlayer()).shape();
                    Block block = chipsBlockEntity.getBlockAtCorner(hitCorner);
                    chipsBlockEntity.toggleDefaultUv(block);
                    playerEntity.swingHand(context.getHand());
                } else {
                    playerEntity.setCurrentHand(context.getHand());
                }
            }
        }

        return ActionResult.CONSUME;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return useTime;
    }

    private HitResult getHitResult(PlayerEntity user) {
        return ProjectileUtil.getCollision(user, EntityPredicates.CAN_HIT, user.getBlockInteractionRange());
    }

    private boolean canChisel(BlockView world, BlockPos pos, Entity user) {
        if (world.getBlockEntity(pos) instanceof ChipsBlockEntity) {
            return true;
        }

        ShapeContext shapeContext = ShapeContext.of(user);
        BlockState state = world.getBlockState(pos);

        if (state.getHardness(world, pos) == -1.0f) {
            return false;
        }

        VoxelShape shape = state.getOutlineShape(world, pos, shapeContext);

        return shape == VoxelShapes.fullCube();
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (use(world, user, stack, remainingUseTicks) == ActionResult.FAIL) {
            user.stopUsingItem();
        }
    }

    private ActionResult use(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) {
            return ActionResult.FAIL;
        }

        HitResult hitResult = getHitResult(player);

        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            return ActionResult.FAIL;
        }

        BlockPos blockPos = blockHitResult.getBlockPos();

        if (!canChisel(world, blockPos, user)) {
            return ActionResult.FAIL;
        }

        if (!world.isClient()) {
            ServerPlayNetworking.send((ServerPlayerEntity) player, new ChiselAnimationPayload(true));
        }

        if ((remainingUseTicks % ANIMATION_TIME) == 0 && remainingUseTicks != 0) {
            Block hoveredBlock = getHoveredBlock(world, blockPos, blockHitResult);
            if (hoveredBlock != null) {
                playHitSound(player, hoveredBlock, world, blockPos); addHitParticles(world, blockHitResult, hoveredBlock, player);
            }
        }

        if (remainingUseTicks != 1) {
            return ActionResult.SUCCESS;
        }

        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        if (!(blockEntity instanceof ChipsBlockEntity chipsBlockEntity)) {
            if (!world.isClient()) {
                BlockState state = world.getBlockState(blockPos);
                Block block = state.getBlock();
                world.setBlockState(blockPos, ChipsBlocks.CHIPS_BLOCK.getDefaultState());

                if (blockEntity != null) {
                    blockEntity.onBlockReplaced(blockPos, state);
                }

                blockEntity = world.getBlockEntity(blockPos);

                if ((!(blockEntity instanceof ChipsBlockEntity chipsBlockEntity))) {
                    return ActionResult.FAIL;
                }

                chipsBlockEntity.addChips(block, 255);
                ServerPlayNetworking.send((ServerPlayerEntity) player, new ChipsBlockChangePayload(blockPos, block));
            }
        } else {
            int corner = ChipsBlock.getClosestSlice(world, blockPos, blockHitResult.getPos()).shape();

            if (corner == chipsBlockEntity.getTotalChips()) {
                chipsBlockEntity.forEachKey(blockType -> destroyChipEffects(
                        player, blockType, world, hitResult.getPos(), blockPos
                ));

                world.breakBlock(blockPos, false);
            } else {
                List<Block> removedCorners = chipsBlockEntity.removeChips(corner);
                removedCorners.forEach(blockType -> destroyChipEffects(
                        player, blockType, world, hitResult.getPos(), blockPos
                ));
            }
        }

        if (!stack.isOf(ChipsItems.CREATIVE_CHISEL_ITEM)) {
            stack.damage(1, player);
        }

        return ActionResult.SUCCESS;
    }

    private @Nullable Block getHoveredBlock(World world, BlockPos pos, BlockHitResult hitResult) {
        BlockState state = world.getBlockState(pos);
        if (!state.isOf(ChipsBlocks.CHIPS_BLOCK)) {
            return state.getBlock();
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof ChipsBlockEntity chipsBlockEntity)) {
            return null;
        }

        CornerInfo corner = ChipsBlock.getClosestSlice(world, pos, hitResult.getPos());
        return chipsBlockEntity.firstBlockWithCorner(corner);
    }

    private void playBreakSound(PlayerEntity player, Block block, World world, BlockPos pos) {
        BlockState state = block.getDefaultState();
        BlockSoundGroup blockSoundGroup = state.getSoundGroup();
        world.playSound(
                player,
                pos,
                blockSoundGroup.getBreakSound(),
                SoundCategory.BLOCKS,
                (blockSoundGroup.getVolume() + 1.0F) / 2.0F,
                blockSoundGroup.getPitch() * 0.8F
        );
        world.addBlockBreakParticles(pos, state);
    }

    // blatantly stolen from BrushItem
    private void addHitParticles(World world, BlockHitResult hitResult, Block blockType, PlayerEntity player) {
        double d = (double)3.0F;
        int i = player.getMainArm() == Arm.RIGHT ? 1 : -1;
        int j = world.getRandom().nextBetweenExclusive(7, 12);
        BlockStateParticleEffect blockStateParticleEffect = new BlockStateParticleEffect(ParticleTypes.BLOCK, blockType.getDefaultState());
        Direction direction = hitResult.getSide();
        BrushItem.DustParticlesOffset dustParticlesOffset = BrushItem.DustParticlesOffset.fromSide(player.getRotationVec(0.0f), direction);
        Vec3d vec3d = hitResult.getPos();

        for(int k = 0; k < j; ++k) {
            world.addParticleClient(blockStateParticleEffect, vec3d.x - (double)(direction == Direction.WEST ? 1.0E-6F : 0.0F), vec3d.y, vec3d.z - (double)(direction == Direction.NORTH ? 1.0E-6F : 0.0F), dustParticlesOffset.xd() * (double)i * (double)3.0F * world.getRandom().nextDouble(), (double)0.0F, dustParticlesOffset.zd() * (double)i * (double)3.0F * world.getRandom().nextDouble());
        }
    }

    private void playHitSound(PlayerEntity player, Block block, World world, BlockPos pos) {
        BlockState state = block.getDefaultState();
        BlockSoundGroup blockSoundGroup = state.getSoundGroup();
        world.playSound(
                player,
                pos,
                blockSoundGroup.getHitSound(),
                SoundCategory.BLOCKS,
                (blockSoundGroup.getVolume() + 1.0F) / 2.0F,
                blockSoundGroup.getPitch() * 0.8F
        );
    }

    private void destroyChipEffects(PlayerEntity player, Block block, World world, Vec3d pos, BlockPos blockPos) {
        playBreakSound(player, block, world, blockPos);
        dropStack(world, ChipsBlockItem.getStack(block), pos);
    }

    private void dropStack(World world, ItemStack stack, Vec3d pos) {
        Random random = world.getRandom();

        ItemEntity itemEntity = new ItemEntity(
                world,
                pos.getX(), pos.getY(), pos.getZ(),
                stack,
                MathHelper.nextDouble(random, -0.125f, 0.125f),
                MathHelper.nextDouble(random, 0.0, 0.125f),
                MathHelper.nextDouble(random, -0.125f, 0.125f)
        );
        itemEntity.setToDefaultPickupDelay();
        world.spawnEntity(itemEntity);
    }

//    private static boolean canChisel(BlockState state, float hardness) {
//        return state.contains(ChipsBlockHelpers.CHIPS) && hardness != -1;
//    }
}