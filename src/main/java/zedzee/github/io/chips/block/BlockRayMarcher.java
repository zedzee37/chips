package zedzee.github.io.chips.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

// i hate that i need this
public class BlockRayMarcher {

    @Nullable
    public static BlockHitResult march(Entity user, double distance, float gridSize) {
        float yaw = user.getHeadYaw();
        float pitch = user.getPitch();

        double distanceTraveled = 0.0f;

        Vec3d direction = Vec3d.fromPolar(pitch, yaw);
        Vec3d position = user.getPos().add(0, user.getEyeHeight(user.getPose()), 0);

        World world = user.getWorld();

        while (distanceTraveled < distance) {
            BlockPos blockPos = BlockPos.ofFloored(position);
            BlockState state = world.getBlockState(blockPos);
            VoxelShape shape = state.getRaycastShape(world, blockPos);

            if (!shape.isEmpty() && shape.getBoundingBox().offset(blockPos).contains(position)) {
                // direction isnt important here
                return new BlockHitResult(position, Direction.NORTH, blockPos, false);
            }

            Vec3d jumpToNextGrid = getVectorToNextGrid(direction, position, gridSize);
            distanceTraveled += jumpToNextGrid.length();

            position = position.add(jumpToNextGrid);
        }

        return null;
    }

    private static Vec3d getVectorToNextGrid(Vec3d direction, Vec3d position, float gridSize) {
        double relX = positiveModulo(position.x, gridSize);
        double relY = positiveModulo(position.y, gridSize);
        double relZ = positiveModulo(position.z, gridSize);

        Vec3d step = new Vec3d(
                direction.x > 0 ? gridSize - relX : (relX == 0 ? 0 : -relX),
                direction.y > 0 ? gridSize - relY : (relY == 0 ? 0 : -relY),
                direction.z > 0 ? gridSize - relZ : (relZ == 0 ? 0 : -relZ)
        );

        Vec3d tValues = new Vec3d(
                safeDiv(step.x, direction.x),
                safeDiv(step.y, direction.y),
                safeDiv(step.z, direction.z)
        );

        double minT = Math.min(tValues.x, Math.min(tValues.y, tValues.z)) + 0.001;
        return direction.normalize().multiply(minT);
    }

    private static double safeDiv(double a, double b) {
        return a / (Math.abs(b) < 1e-6 ? 1e-6 : b);
    }

    private static double positiveModulo(double value, double modulus) {
        double result = value % modulus;
        return result < 0 ? result + modulus : result;
    }
}
