package zedzee.github.io.chips.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.block.entity.ChipsBlockEntity;
import zedzee.github.io.chips.component.BlockComponent;
import zedzee.github.io.chips.component.ChipsComponents;

public class ChipsBlockItem extends BlockItem {
    private final static float EPSILON = 0.01f;

    public ChipsBlockItem(Settings settings) {
        super(ChipsBlocks.CHIPS_BLOCK, settings);
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack defaultStack = super.getDefaultStack();
        defaultStack.set(ChipsComponents.BLOCK_COMPONENT_COMPONENT, new BlockComponent(Blocks.DIAMOND_BLOCK));
        return defaultStack;
    }

    @Override
    public ActionResult place(ItemPlacementContext context) {
        ItemStack stack = context.getStack();
        if (!stack.contains(ChipsComponents.BLOCK_COMPONENT_COMPONENT)) {
            return ActionResult.FAIL;
        }

        Block blockType = stack.get(ChipsComponents.BLOCK_COMPONENT_COMPONENT).block();

        World world = context.getWorld();
        Vec3d hitPos = context.getHitPos();
        BlockPos pos = new BlockPos(
                (int) hitPos.getX(),
                (int) hitPos.getY(),
                (int) hitPos.getZ()
        );

        Vec3d adjustedHitPos = hitPos.subtract(pos.getX(), pos.getY(), pos.getZ());
        int corner = getTargetCorner(adjustedHitPos);

        BlockState state = world.getBlockState(pos);

        ActionResult result;

        if (state.isOf(ChipsBlocks.CHIPS_BLOCK)) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (!(entity instanceof ChipsBlockEntity chipsBlockEntity)) {
                return ActionResult.FAIL;
            }

            if (chipsBlockEntity.hasCorner(corner)) {
                Chips.LOGGER.info(hitPos.toString());
                Chips.LOGGER.info(pos.toString());
                Vec3d direction = Vec3d.of(pos).subtract(hitPos).normalize().multiply(EPSILON);
                adjustedHitPos = adjustedHitPos.add(direction);

                corner = getTargetCorner(adjustedHitPos);

                if (chipsBlockEntity.hasCorner(corner)) {
                    return ActionResult.FAIL;
                }
            }
            Chips.LOGGER.info(hitPos.toString());
            Chips.LOGGER.info(pos.toString());
            chipsBlockEntity.addChips(blockType, corner);
            result = ActionResult.SUCCESS;
        } else {
            result = tryPlaceAt(context);
        }

        if (result == ActionResult.SUCCESS) {
            playPlaceSound(world, context.getPlayer(), blockType, context.getBlockPos());
            stack.decrementUnlessCreative(1, context.getPlayer());
        }

        return result;
    }

    public ActionResult tryPlaceAt(ItemPlacementContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        Vec3d hitPos = context.getHitPos();
        Vec3d adjustedHitPos = hitPos.subtract(Vec3d.of(pos));

        int corner = getTargetCorner(adjustedHitPos);

        BlockState state = world.getBlockState(pos);
        if (!state.isAir() && !state.isOf(ChipsBlocks.CHIPS_BLOCK)) {
            return ActionResult.FAIL;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null) {
            world.setBlockState(pos, ChipsBlocks.CHIPS_BLOCK.getDefaultState());
            blockEntity = world.getBlockEntity(pos);
        }

        if (!(blockEntity instanceof ChipsBlockEntity chipsBlockEntity)) {
            return ActionResult.FAIL;
        }

        Block blockType = context.getStack().get(ChipsComponents.BLOCK_COMPONENT_COMPONENT).block();

        if (chipsBlockEntity.hasCorner(corner)) {
            Vec3d direction = Vec3d.of(pos).subtract(hitPos).normalize().multiply(EPSILON);
            adjustedHitPos = adjustedHitPos.add(direction);
            corner = getTargetCorner(adjustedHitPos);

            if (chipsBlockEntity.hasCorner(corner)) {
                return ActionResult.FAIL;
            }
        }

        chipsBlockEntity.addChips(blockType, corner);
        return ActionResult.SUCCESS;
    }

    private void playPlaceSound(World world, PlayerEntity playerEntity, Block blockType, BlockPos blockPos) {
        BlockState state = blockType.getDefaultState();
        BlockSoundGroup blockSoundGroup = state.getSoundGroup();
        world.playSound(
                playerEntity,
                blockPos,
                this.getPlaceSound(state),
                SoundCategory.BLOCKS,
                (blockSoundGroup.getVolume() + 1.0F) / 2.0F,
                blockSoundGroup.getPitch() * 0.8F
        );
    }

    public int getTargetCorner(Vec3d relHitPos) {
        int currentCorner = 255;

        if (relHitPos.getX() >= 0.5f) {
            currentCorner &= ~1;
            currentCorner &= ~(1 << 2);

            currentCorner &= ~(1 << 4);
            currentCorner &= ~(1 << 6);
        } else {
            currentCorner &= ~(1 << 1);
            currentCorner &= ~(1 << 3);

            currentCorner &= ~(1 << 5);
            currentCorner &= ~(1 << 7);
        }

        if (relHitPos.getZ() >= 0.5f) {
            currentCorner &= ~1;
            currentCorner &= ~2;

            currentCorner &= ~(1 << 4);
            currentCorner &= ~(1 << 5);
        } else {
            currentCorner &= ~4;
            currentCorner &= ~8;

            currentCorner &= ~(1 << 6);
            currentCorner &= ~(1 << 7);
        }

        if (relHitPos.getY() >= 0.5f) {
            currentCorner &= ~(1 | 2 | 4 | 8);
        } else {
            currentCorner &= ~(16 | 32 | 64 | 128);
        }

        return currentCorner;
    }
}
