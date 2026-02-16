package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
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
    private final Mesh mesh;

    public ChipsBlockBreakingBakedModel(CornerInfo corner) {
        super();

        Box box = ChipsBlock.getShape(corner.shape()).getBoundingBox();
        Vec3d min = box.getMinPos();
        Vec3d max = box.getMaxPos();

        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        MeshBuilder meshBuilder = renderer.meshBuilder();
        QuadEmitter emitter = meshBuilder.getEmitter();

        for (Direction direction : Direction.values()) {
            ChipsBlockModel.emitQuadPositions(
                    emitter,
                    direction,
                    (float)min.x,
                    (float)min.y,
                    (float)min.z,
                    (float)max.x,
                    (float)max.y,
                    (float)max.z);
            emitter.color(-1, -1, -1, -1);
            setUv(emitter);
            emitter.cullFace(null);
            emitter.emit();
        }

        this.mesh = meshBuilder.build();
    }

    // this function sucks
    private static void setUv(QuadEmitter emitter) {
        final int minU = 0;
        final int maxU = 8;

        final int minV = 8;
        final int maxV = 0;

        emitter.uv(0, maxU, maxV);
        emitter.uv(1, maxU, minV);
        emitter.uv(2, minU, minV);
        emitter.uv(3, minU, maxV);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        List<BakedQuad> quadList = new ArrayList<>(4);
        this.mesh.forEach(quad -> quadList.add(quad.toBakedQuad(null)));
        return quadList;
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
