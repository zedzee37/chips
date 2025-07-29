package zedzee.github.io.chips.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import java.util.Optional;

public class ChipsBlock extends Block {
    public static final IntProperty CHIPS_PROPERTY = IntProperty.of("chips", 0, 255);

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

    public ChipsBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(CHIPS_PROPERTY, 255));
    }

    private static boolean hasCorner(int flags, int corner) {
        return (flags & createFlag(corner)) != 0;
    }

    private static int createFlag(int corner) {
        return 1 << corner;
    }

    private static boolean isFull(BlockState state) {
        return state.get(CHIPS_PROPERTY) == 255;
    }

    public int getClosestSlice(BlockState state, Vec3d pos) {
        int i = state.get(CHIPS_PROPERTY);
        double d = Double.MAX_VALUE;
        int j = -1;

        for (int k = 0; k < CORNER_SHAPES.length; k++) {
            if (hasCorner(i, k)) {
                VoxelShape voxelShape = CORNER_SHAPES[k];
                Optional<Vec3d> optional = voxelShape.getClosestPointTo(pos);
                if (optional.isPresent()) {
                    double e = (optional.get()).squaredDistanceTo(pos);
                    if (e < d) {
                        d = e;
                        j = k;
                    }
                }
            }
        }

        return j;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES[state.get(CHIPS_PROPERTY)];
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return isFull(state) ? 0.2F : 1.0F;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(CHIPS_PROPERTY);
    }
}
