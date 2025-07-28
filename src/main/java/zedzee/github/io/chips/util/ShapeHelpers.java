package zedzee.github.io.chips.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import java.util.Optional;

public class ShapeHelpers {
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

    private static boolean hasCorner(int flags, int corner) {
        return (flags & createFlag(corner)) != 0;
    }

    private static int createFlag(int corner) {
        return 1 << corner;
    }

    public static int getClosestSlice(BlockState state, Vec3d pos) {
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

    public static VoxelShape getOutlineShape(BlockState state) {
        return SHAPES[state.get(CHIPS_PROPERTY)];
    }
}
