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
        this.setDefaultState(this.getDefaultState().with(CHIPS_PROPERTY, 1));
    }

    private static int getBottomLayer(int n) {
        return (n & 0x0F);
    }

    private static int getTopLayer(int n) {
        return (n & 0xF0) >> 4;
    }

    private static int rotate4Left(int n) {
        return (
                ((n & 1) << 2) |
                ((n & 0b10) >> 1) |
                ((n & 0b100) << 1) |
                ((n & 0b1000) >> 2)
        );
    }

    private static int rotate4Right(int n) {
        return (
                ((n & 1) << 1) |
                ((n & 0b10) << 2) |
                ((n & 0b100) >> 2) |
                ((n & 0b1000) >> 1)
        );
    }

    private static int rotate8Left(int n) {
        return rotate4Left(getBottomLayer(n)) | (rotate4Left(getTopLayer(n) >> 4));
    }

    private static int rotate8Right(int n) {
        return rotate4Right(getBottomLayer(n)) | (rotate4Right(getTopLayer(n) >> 4));
    }

    private static int simplifyModel(int n) {
        int leadingZeros = Integer.numberOfLeadingZeros(n);
        if (leadingZeros == 4 || leadingZeros == 0) {
            return n;
        }

        for (int i = 0; i < leadingZeros; i++) {
            n = rotate8Left(n);
        }

        return n;
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

    public static int getClosestSlice(BlockState state, Vec3d pos) {
        int i = 255;
        if (state.contains(CHIPS_PROPERTY)) {
            i = state.get(CHIPS_PROPERTY);
        }
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
