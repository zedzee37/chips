package zedzee.github.io.chips.client.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.block.CornerInfo;

import java.util.ArrayList;
import java.util.List;

// this kinda sucks but whatever
public class ChipsBlockBreakingBakedModel implements BakedModel {
    private static final Direction[] DIRECTIONS = Direction.values();

    private CornerInfo corner;

    public void BlockBreakingModel(CornerInfo corner) {
        this.corner = corner;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        List<BakedQuad> quadList = new ArrayList<>(4);
        VoxelShape shape = ChipsBlock.getShape(corner.shape());
        Box box = shape.getBoundingBox();
        Vec3d minPos = box.getMinPos();
        Vec3d maxPos = box.getMaxPos();

        for (int i = 0; i < DIRECTIONS.length; i++) {
            Direction direction = DIRECTIONS[i];

            int[] vertices = populateVertices(minPos, maxPos, direction);


        }

        return quadList;
    }

    public int[] populateVertices(Vec3d minPos, Vec3d maxPos, Direction direction) {
        // 12 for each x, y, z
        int[] vertices = new int[4 * 3];

        switch (direction) {
            case SOUTH:
            case NORTH:
                break;
            case WEST:
            case EAST:
                break;
            case DOWN:
            case UP:
                break;
        }

        return vertices;
    }

    public void addVertex(int[] vertices, int startPos, Vec3d vertex) {
        vertices[startPos] = (int)vertex.x;
        vertices[startPos + 1] = (int)vertex.y;
        vertices[startPos + 2] = (int)vertex.z;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean hasDepth() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getParticleSprite() {
        return null;
    }

    @Override
    public ModelTransformation getTransformation() {
        return ModelTransformation.NONE;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }
}
