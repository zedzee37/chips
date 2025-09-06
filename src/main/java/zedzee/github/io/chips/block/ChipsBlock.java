package zedzee.github.io.chips.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.entity.ChipsBlockEntity;
import zedzee.github.io.chips.component.ChipsComponents;
import zedzee.github.io.chips.item.ChipsBlockItem;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChipsBlock extends BlockWithEntity implements Waterloggable {
    public static final int DEFAULT_CHIPS_VALUE = 1;

    public static final VoxelShape[] CORNER_SHAPES = Util.make(new VoxelShape[8], cornerShapes -> {
        cornerShapes[0] = VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.5, 0.5, 0.5);
        cornerShapes[1] = VoxelShapes.cuboid(0.5, 0.0, 0.0, 1.0, 0.5, 0.5);
        cornerShapes[2] = VoxelShapes.cuboid(0.0, 0.0, 0.5, 0.5, 0.5, 1.0);
        cornerShapes[3] = VoxelShapes.cuboid(0.5, 0.0, 0.5, 1.0, 0.5, 1.0);
        cornerShapes[4] = VoxelShapes.cuboid(0.0, 0.5, 0.0, 0.5, 1.0, 0.5);
        cornerShapes[5] = VoxelShapes.cuboid(0.5, 0.5, 0.0, 1.0, 1.0, 0.5);
        cornerShapes[6] = VoxelShapes.cuboid(0.0, 0.5, 0.5, 0.5, 1.0, 1.0);
        cornerShapes[7] = VoxelShapes.cuboid(0.5, 0.5, 0.5, 1.0, 1.0, 1.0);
    });

    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public static final VoxelShape[] SHAPES = Util.make(new VoxelShape[256], voxelShapes -> {
        for (int i = 0; i < voxelShapes.length; i++) {
            VoxelShape voxelShape = VoxelShapes.empty();

            for (int j = 0; j < 8; j++) {
                if (hasCorner(i, j)) {
                    voxelShape = VoxelShapes.union(voxelShape, CORNER_SHAPES[j]);
                }
            }

            voxelShapes[i] = voxelShape.simplify();
        }
    });

    public static boolean hasCorner(int flags, int corner) {
        return (flags & createFlag(corner)) != 0;
    }

    private static int createFlag(int corner) {
        return 1 << corner;
    }

    public static int countCorners(int flags) {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            count += ((1 << i) & flags) != 0 ? 1 : 0;
        }
        return count;
    }

    public boolean isFull(BlockPos pos, BlockView world) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ChipsBlockEntity chipsBlockEntity) {
            return chipsBlockEntity.getTotalChips() == 255;
        }
        return true;
    }

    // WARNING: RETURNS THE CORNER INDEX, NOT THE SHAPE!! USE 1 << <return value> FOR THE SHAPE INDEX!!
    public static int getHoveredCorner(BlockView world, PlayerEntity player) {
        int corner = -1;
        BlockHitResult blockHitResult = BlockRayMarcher.march(player, player.getBlockInteractionRange(), 0.125f);

        if (blockHitResult == null) {
            return corner;
        }

        return getClosestSlice(world, blockHitResult.getBlockPos(), blockHitResult.getPos());
    }

    // WARNING: RETURNS THE CORNER INDEX, NOT THE SHAPE!! USE 1 << <return value> FOR THE SHAPE INDEX!!
    public static int getClosestSlice(BlockView view, BlockPos pos, Vec3d hitPos) {
        hitPos = hitPos.subtract(Vec3d.of(pos));

        BlockEntity entity = view.getBlockEntity(pos);
        if (!(entity instanceof ChipsBlockEntity chipsBlockEntity)) {
            return -1;
        }

        int i = chipsBlockEntity.getTotalChips();
        double d = Double.MAX_VALUE;
        int j = -1;

        for (int k = 0; k < CORNER_SHAPES.length; k++) {
            if (hasCorner(i, k)) {
                VoxelShape voxelShape = CORNER_SHAPES[k];
                Optional<Vec3d> optional = voxelShape.getClosestPointTo(hitPos);
                if (optional.isPresent()) {
                    double e = (optional.get()).squaredDistanceTo(hitPos);
                    if (e < d) {
                        d = e;
                        j = k;
                    }
                }
            }
        }

        return j;
    }

    public ChipsBlock(Settings settings) {
        super(settings);
        setDefaultState(
                getDefaultState()
                        .with(WATERLOGGED, false)
        );
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(ChipsBlock::new);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ChipsBlockEntity(pos, state);
    }

//    public static Optional<Integer> getChips(BlockPos pos, BlockView world) {
//        return
//                findBlockEntity(pos, world)
//                        .map(ChipsBlockEntity::getChips);
//    }
//
//    public static void setChips(BlockPos pos, BlockView world, int chips) {
//        findBlockEntity(pos, world)
//                .ifPresent(entity -> entity.setChips(chips));
//
//    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = getCollisionShape(state, world, pos, context);

        if (context instanceof EntityShapeContext entityShapeContext &&
                entityShapeContext.getEntity() instanceof PlayerEntity player &&
                player.getMainHandStack().contains(ChipsComponents.INDIVIDUAL_CHIPS_COMPONENT_COMPONENT)
        ) {
            int corner = getHoveredCorner(world, player);

            if (corner == -1) {
                return shape;
            }

            return getShape(1 << corner);
        }
        return shape;
    }

//    @Override
//    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
//        BlockEntity blockEntity = world.getBlockEntity(pos);
//        if (blockEntity instanceof ChipsBlockEntity chipsBlockEntity) {
//            return getShape(chipsBlockEntity.getTotalChips());
//        }
//
//        return VoxelShapes.empty();
//    }


    @Override
    protected List<ItemStack> getDroppedStacks(BlockState state, LootWorldContext.Builder builder) {
        BlockEntity blockEntity = builder.get(LootContextParameters.BLOCK_ENTITY);
        if (!(blockEntity instanceof ChipsBlockEntity chipsBlockEntity)) {
            return List.of();
        }

        ArrayList<ItemStack> stacks = new ArrayList<>();
        chipsBlockEntity.forEachKey(block -> {
            int chips = chipsBlockEntity.getChips(block);

            ItemStack stack = ChipsBlockItem.getStack(block);
            int cornerCount = countCorners(chips);
            stack.setCount(cornerCount);

            stacks.add(stack);
        });

        return stacks;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ChipsBlockEntity chipsBlockEntity) {
            return getShape(chipsBlockEntity.getTotalChips());
        }

        return VoxelShapes.empty();
    }

    @Override
    protected VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return getCollisionShape(state, world, pos, ShapeContext.absent());
    }

    public static VoxelShape getShape(int chips) {
        return SHAPES[chips];
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }

//    @Override
//    protected boolean isTransparent(BlockState state) {
//        return true;
//    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return isFull(pos, world) ? 0.2F : 1.0F;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state,
                                                WorldView world,
                                                ScheduledTickView tickView,
                                                BlockPos pos,
                                                Direction direction,
                                                BlockPos neighborPos,
                                                BlockState neighborState,
                                                Random random) {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }
}
