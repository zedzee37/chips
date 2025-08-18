package zedzee.github.io.chips.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.block.entity.ChipsBlockEntity;
import zedzee.github.io.chips.component.BlockComponent;
import zedzee.github.io.chips.component.ChipsComponents;

public class ChipsBlockItem extends BlockItem {
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
                (int) Math.floor(hitPos.getX()),
                (int) Math.floor(hitPos.getY()),
                (int) Math.floor(hitPos.getZ())
        );

        Vec3d adjustedHitPos = hitPos.subtract(pos.getX(), pos.getY(), pos.getZ());
        int corner = getTargetCorner(adjustedHitPos);

        BlockState state = world.getBlockState(pos);

        Chips.LOGGER.info(state.toString());

        if (state.isOf(ChipsBlocks.CHIPS_BLOCK)) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (!(entity instanceof ChipsBlockEntity chipsBlockEntity)) {
                return ActionResult.FAIL;
            }

            if (chipsBlockEntity.hasCorner(corner)) {
                adjustedHitPos = adjustedHitPos.subtract(new Vec3d(0.1, 0.1, 0.1));
                corner = getTargetCorner(adjustedHitPos);
                if (chipsBlockEntity.hasCorner(corner)) {
                    return ActionResult.FAIL;
                }
            }

            Chips.LOGGER.info("gug");
            chipsBlockEntity.addChips(blockType, corner);
        }

        return ActionResult.SUCCESS;
//
//
//        World world = context.getWorld();
//        BlockPos pos = context.getBlockPos();
//        Vec3d hitPos = context.getHitPos();
//
//        if (world.getBlockState(pos).isOf(ChipsBlocks.CHIPS_BLOCK)) {
//            BlockEntity blockEntity = world.getBlockEntity(pos);
//
//            if (!(blockEntity instanceof ChipsBlockEntity chipsBlockEntity)) {
//                return ActionResult.FAIL;
//            }
//
//            Vec3d relativeHitPos = hitPos.subtract(pos.getX(), pos.getY(), pos.getZ());
//            int corner = getTargetCorner(relativeHitPos);
//
//            if (chipsBlockEntity.hasCorner(corner)) {
//                return ActionResult.FAIL; // Corner already occupied
//            }
//
//            chipsBlockEntity.addChips(blockType, corner);
//            return ActionResult.SUCCESS;
//        }

//        boolean shouldClear = false;
//        if (!world.getBlockState(asBlockPos).isOf(ChipsBlocks.CHIPS_BLOCK)) {
//            world.setBlockState(pos, getBlock().getDefaultState());
//            shouldClear = true;
//        }
//
//        ChipsBlockEntity chipsBlockEntity = (ChipsBlockEntity)world.getBlockEntity(asBlockPos);
//        if (shouldClear) {
//            chipsBlockEntity.clear();
//        }
//
//        Block blockType = stack.get(ChipsComponents.BLOCK_COMPONENT_COMPONENT).block();
//
//        // block coords
//
//        chipsBlockEntity.setChips(blockType, corner);
//
//        return ActionResult.SUCCESS;
    }

//    public ActionResult tryPlaceAt(ItemPlacementContext context) {
//
//    }

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

//    private Vec3d correctPos(Vec3d pos, int totalChips) {
//        double correctedX = pos.getX();
//        double correctedY = pos.getY();
//        double correctedZ = pos.getZ();
//
//        if (correctedX == 0.5f) {
//
//        }
//
//        if (correctedZ == 0.5f) {
//
//        }
//
//        if (correctedZ == 0.5f) {
//
//        }
//
//        return new Vec3d(correctedX, correctedY, correctedZ);
//    }
}
