package zedzee.github.io.chips.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.DirectionTransformation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.Optional;

public class ChipsBlockHelpers {
    public static final IntProperty CHIPS = IntProperty.of("chips", 0, 255);
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;

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

    public static int simplifyModel(int n) {
        int leadingZeros = Integer.numberOfLeadingZeros(n);
        if (leadingZeros == 4 || leadingZeros == 0) {
            return n;
        }

        for (int i = 0; i < leadingZeros; i++) {
            n = rotate8Left(n);
        }

        return n;
    }

    public static boolean hasCorner(int flags, int corner) {
        return (flags & createFlag(corner)) != 0;
    }

    private static int createFlag(int corner) {
        return 1 << corner;
    }

    public static boolean isFull(BlockState state) {
        return state.get(CHIPS) == 255;
    }

    public static int getClosestSlice(BlockState state, Vec3d pos) {
        int i = 255;
        if (state.contains(CHIPS)) {
            i = state.get(CHIPS);
        }
        double d = Double.MAX_VALUE;
        int j = -1;

        for (int k = 0; k < CORNER_SHAPES.length; k++) {
            if (hasCorner(i, k)) {
                VoxelShape voxelShape = rotateShape(CORNER_SHAPES[k], state.get(FACING));
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

    public static VoxelShape getOutlineShape(BlockState state) {
        VoxelShape shape = SHAPES[state.get(CHIPS)];
        Direction direction = state.get(FACING);
        return rotateShape(shape, direction);
    }

    private static VoxelShape rotateShape(VoxelShape shape, Direction direction) {
        switch (direction) {
            // hacky way to rotate it 180 ig
            case SOUTH -> shape = VoxelShapes.transform(VoxelShapes.transform(shape, DirectionTransformation.ROT_90_Y_POS), DirectionTransformation.ROT_90_Y_POS);
            case EAST -> shape = VoxelShapes.transform(shape, DirectionTransformation.ROT_90_Y_NEG);
            case WEST -> shape = VoxelShapes.transform(shape, DirectionTransformation.ROT_90_Y_POS);
        }

        return shape;
    }
}
