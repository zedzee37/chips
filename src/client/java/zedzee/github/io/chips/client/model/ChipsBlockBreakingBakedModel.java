package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat;
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
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.block.CornerInfo;

import java.util.ArrayList;
import java.util.List;

// this kinda sucks but whatever
public class ChipsBlockBreakingBakedModel implements BakedModel, FabricBakedModel {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int[] EMPTY = new int[EncodingFormat.TOTAL_STRIDE];
    private static final int VERTEX_COLOR = EncodingFormat.HEADER_STRIDE + 3;

    private final Vec3d min;
    private final Vec3d max t;

    public ChipsBlockBreakingBakedModel(CornerInfo corner) {
        super();
        Box box = ChipsBlock.getShape(corner.shape()).getBoundingBox();
        this.min = box.getMinPos();
        this.max = box.getMaxPos();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        List<BakedQuad> quadList = new ArrayList<>(4);

        for (Direction direction : DIRECTIONS) {
            int[] vertices = populateVertices(min, max, direction);
            BakedQuad quad = new BakedQuad(vertices, -1, direction, null, false);

            quadList.add(quad);
        }

        return quadList;
    }

    public int[] populateVertices(Vec3d minPos, Vec3d maxPos, Direction direction) {
        int[] vertices = new int[EncodingFormat.TOTAL_STRIDE];
        System.arraycopy(EMPTY, 0, vertices, 0, EncodingFormat.TOTAL_STRIDE);

        Vec3d tmp = minPos;
        switch (direction) {
            case SOUTH:
                minPos = maxPos;
                maxPos = tmp;
            case NORTH:
                addVertex(vertices, 0, maxPos.getX(), maxPos.getY(), minPos.getZ());
                addVertex(vertices, 1, maxPos.getX(), minPos.getY(), minPos.getZ());
                addVertex(vertices, 2, minPos.getX(), maxPos.getY(), minPos.getZ());
                addVertex(vertices, 3, minPos.getX(), minPos.getY(), minPos.getZ());
                break;
            case WEST:
                minPos = maxPos;
                maxPos = tmp;
            case EAST:
                addVertex(vertices, 0, maxPos.getX(), maxPos.getY(), maxPos.getZ());
                addVertex(vertices, 1, maxPos.getX(), minPos.getY(), maxPos.getZ());
                addVertex(vertices, 2, maxPos.getX(), minPos.getY(), minPos.getZ());
                addVertex(vertices, 3, maxPos.getX(), maxPos.getY(), minPos.getZ());
                break;
            case UP:
                minPos = maxPos;
                maxPos = tmp;
            case DOWN:
                addVertex(vertices, 0, minPos.getX(), minPos.getY(), minPos.getZ());
                addVertex(vertices, 1, maxPos.getX(), minPos.getY(), minPos.getZ());
                addVertex(vertices, 2, maxPos.getX(), minPos.getY(), maxPos.getZ());
                addVertex(vertices, 3, minPos.getX(), minPos.getY(), maxPos.getZ());
                break;
        }

        return vertices;
    }

    public void addVertex(int[] vertices, int startPos, double x, double y, double z) {
        startPos = startPos * EncodingFormat.VERTEX_STRIDE + EncodingFormat.HEADER_STRIDE;
        vertices[startPos] = (int)x;
        vertices[startPos + 1] = (int)y;
        vertices[startPos + 2] = (int)z;
        vertices[startPos * EncodingFormat.VERTEX_STRIDE + VERTEX_COLOR] = -1;
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
