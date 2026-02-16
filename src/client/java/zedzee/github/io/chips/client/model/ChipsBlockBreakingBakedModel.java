package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.material.ShadeMode;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.block.CornerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// this kinda sucks but whatever
public class ChipsBlockBreakingBakedModel implements BakedModel, FabricBakedModel {
    private static RenderMaterial renderMaterial;

    private final Mesh mesh;

    public ChipsBlockBreakingBakedModel(CornerInfo corner) {
        super();

        Box box = ChipsBlock.getShape(corner.shape()).getBoundingBox();
        Vec3d min = box.getMinPos();
        Vec3d max = box.getMaxPos();

        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        MeshBuilder meshBuilder = renderer.meshBuilder();
        QuadEmitter emitter = meshBuilder.getEmitter();

        if (renderMaterial == null) {
            MaterialFinder finder = renderer.materialFinder();
            renderMaterial = finder
                    .ambientOcclusion(TriState.TRUE)
                    .disableDiffuse(false)
                    .shadeMode(ShadeMode.VANILLA)
                    .blendMode(BlendMode.TRANSLUCENT)
                    .find();
        }

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
            // this does nothing
            int color = ColorHelper.Argb.getArgb(0, 255, 255, 255);
            emitter.color(color, color, color, color);
            setUv(emitter);
            emitter.material(renderMaterial);
            emitter.cullFace(null);
            emitter.nominalFace(direction);
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
        return List.of();
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        mesh.outputTo(context.getEmitter());
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean hasDepth() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return true;
    }

    @Override
    public boolean isVanillaAdapter() {
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
