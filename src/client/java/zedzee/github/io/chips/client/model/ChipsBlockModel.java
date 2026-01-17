package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.*;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.component.ChipsBlockItemComponent;
import zedzee.github.io.chips.component.ChipsComponents;
import zedzee.github.io.chips.item.ChipsItems;
import zedzee.github.io.chips.render.RenderData;
import zedzee.github.io.chips.util.RandomSupplier;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

// i swear ill change this name
public class ChipsBlockModel implements UnbakedModel, BakedModel, FabricBakedModel {
    private static final SpriteIdentifier FALLBACK_PARTICLE_SPRITE = new SpriteIdentifier(
            PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("block/dirt")
    );

    private Supplier<BakeArgs> bakeArgsSupplier;
    private RandomSupplier<Sprite> particleSpriteSupplier;
    private Sprite fallbackParticleSprite;
    private RenderMaterial renderMaterialNormal;
    private RenderMaterial renderMaterialTranslucent;

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return List.of();
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
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return true;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getParticleSprite() {
        if (this.particleSpriteSupplier == null) {
            return this.fallbackParticleSprite;
        }

        Sprite randomSprite = particleSpriteSupplier.get();
        return randomSprite == null ? this.fallbackParticleSprite : randomSprite;
    }

    @Override
    public ModelTransformation getTransformation() {
        return ModelHelper.MODEL_TRANSFORM_BLOCK;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return List.of();
    }

    @Override
    public void setParents(Function<Identifier, UnbakedModel> modelLoader) {}

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        if (!stack.isOf(ChipsItems.CHIPS_BLOCK_ITEM) || stack.contains(ChipsComponents.BLOCK_COMPONENT_COMPONENT)) {
            return;
        }

        ChipsBlockItemComponent blockItemComponent = stack.get(ChipsComponents.BLOCK_COMPONENT_COMPONENT);
        assert blockItemComponent != null;
        ChipsItemRenderData renderData = new ChipsItemRenderData(blockItemComponent.block());
        emitModelQuads(context.getEmitter(), randomSupplier.get(), renderData);
    }

    @Override
    public void emitBlockQuads(
            BlockRenderView blockView,
            BlockState state,
            BlockPos pos,
            Supplier<Random> randomSupplier,
            RenderContext context
    ) {
        Object object = blockView.getBlockEntityRenderData(pos);

        if (object != null && !(object instanceof RenderData)) {
            return;
        }

        RenderData renderData = (RenderData)object;
        assert renderData != null;
        emitModelQuads(context.getEmitter(), randomSupplier.get(), renderData);
    }

    private void emitModelQuads(QuadEmitter emitter, Random random, RenderData renderData) {
        final BakeArgs bakeArgs = bakeArgsSupplier.get();

        if (particleSpriteSupplier == null) {
            particleSpriteSupplier = new RandomSupplier<>(random, List.of());
        }

        final Set<Block> blocks = renderData.getBlocks();
        blocks.forEach(block -> {
            final Identifier blockId = Registries.BLOCK.getId(block);
            final Identifier modelId = blockId.withPath(path -> "block/" + path);

            final UnbakedModel model = bakeArgs.baker().getOrLoadModel(modelId);
            final BakedModel blockModel = model.bake(bakeArgs.baker(), bakeArgs.textureGetter(), bakeArgs.modelBakeSettings());

            if (blockModel == null) {
                return;
            }

            final int chips = renderData.getChips(block);
            final VoxelShape shape = ChipsBlock.getShape(chips);

            final BlockState defaultState = block.getDefaultState();
            shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
                for (Direction direction : Direction.values()) {
                    List<BakedQuad> quads = blockModel.getQuads(defaultState, direction, random);
                    for (BakedQuad quad : quads) {
                        emitQuadPositions(
                                emitter,
                                direction,
                                (float)minX,
                                (float)minY,
                                (float)minZ,
                                (float)maxX,
                                (float)maxY,
                                (float)maxZ);
                        applySprite(
                                emitter,
                                quad.getSprite(),
                                direction,
                                (float)minX,
                                (float)minY,
                                (float)minZ,
                                (float)maxX,
                                (float)maxY,
                                (float)maxZ,
                                renderData.shouldUseDefaultUv(block)
                        );
                        emitter.nominalFace(direction);
                        emitter.cullFace(null);
                        emitter.colorIndex(quad.getColorIndex());
                        emitter.color(-1, -1, -1, -1);
                        emitter.tag(0);
                        emitter.emit();
                    }
                }
            });

        });
    }

    @Override
    public @Nullable BakedModel bake(
            Baker baker,
            Function<SpriteIdentifier, Sprite> textureGetter,
            ModelBakeSettings rotationContainer
    ) {
        this.bakeArgsSupplier = () -> new BakeArgs(baker, textureGetter, rotationContainer);
        this.fallbackParticleSprite = textureGetter.apply(FALLBACK_PARTICLE_SPRITE);

        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        MaterialFinder finder = renderer.materialFinder();

        renderMaterialNormal = finder
                .ambientOcclusion(TriState.TRUE)
                .disableDiffuse(false)
                .blendMode(BlendMode.CUTOUT_MIPPED)
                .find();
        renderMaterialTranslucent = finder
                .ambientOcclusion(TriState.TRUE)
                .disableDiffuse(false)
                .blendMode(BlendMode.TRANSLUCENT)
                .find();

        return this;
    }

    private void emitQuadPositions(
            QuadEmitter emitter,
            Direction direction,
            float fromX,
            float fromY,
            float fromZ,
            float toX,
            float toY,
            float toZ
    ) {
        switch (direction) {
            case UP:
                emitter.pos(3, fromX, toY, fromZ);
                emitter.pos(2, toX, toY, fromZ);
                emitter.pos(1, toX, toY, toZ);
                emitter.pos(0, fromX, toY, toZ);
                break;
            case DOWN:
                emitter.pos(0, fromX, fromY, fromZ);
                emitter.pos(1, toX, fromY, fromZ);
                emitter.pos(2, toX, fromY, toZ);
                emitter.pos(3, fromX, fromY, toZ);
                break;
            case NORTH:
                emitter.pos(3, fromX, fromY, fromZ);
                emitter.pos(2, toX, fromY, fromZ);
                emitter.pos(1, toX, toY, fromZ);
                emitter.pos(0, fromX, toY, fromZ);
                break;
            case SOUTH:
                emitter.pos(0, fromX, fromY, toZ);
                emitter.pos(1, toX, fromY, toZ);
                emitter.pos(2, toX, toY, toZ);
                emitter.pos(3, fromX, toY, toZ);
                break;
            case EAST:
                emitter.pos(3, toX, fromY, toZ);
                emitter.pos(2, toX, toY, toZ);
                emitter.pos(1, toX, toY, fromZ);
                emitter.pos(0, toX, fromY, fromZ);
                break;
            case WEST:
                emitter.pos(0, fromX, fromY, toZ);
                emitter.pos(1, fromX, toY, toZ);
                emitter.pos(2, fromX, toY, fromZ);
                emitter.pos(3, fromX, fromY, fromZ);
                break;
        }
    }

    private void applySprite(
            QuadEmitter emitter,
            Sprite sprite,
            Direction direction,
            float fromX,
            float fromY,
            float fromZ,
            float toX,
            float toY,
            float toZ,
            boolean defaultUv
    ) {
        emitter.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV);

        if (defaultUv) {
            return;
        }

        float minU = sprite.getMinU();
        float maxU = sprite.getMaxU();

        float minV = sprite.getMaxV();
        float maxV = sprite.getMinV();

        float x0 = MathHelper.lerp(fromX, minU, maxU);
        float x1 = MathHelper.lerp(toX, minU, maxU);

        float height = Math.abs(toY - fromY);
        float y0 = MathHelper.lerp(1 - height, minV, maxV);

        float z0 = MathHelper.lerp(fromZ, minU, maxU);
        float z1 = MathHelper.lerp(toZ, minU, maxU);

        switch (direction) {
            case NORTH -> {
                emitter.uv(0, x0, maxV);
                emitter.uv(1, x1, maxV);
                emitter.uv(2, x1, y0);
                emitter.uv(3, x0, y0);
            }
            case SOUTH -> {
                emitter.uv(3, x0, maxV);
                emitter.uv(2, x1, maxV);
                emitter.uv(1, x1, y0);
                emitter.uv(0, x0, y0);
            }
            case EAST ->  {
                emitter.uv(0, z0, y0);
                emitter.uv(1, z0, maxV);
                emitter.uv(2, z1, maxV);
                emitter.uv(3, z1, y0);
            }
            case WEST -> {
                emitter.uv(3, z0, y0);
                emitter.uv(2, z0, maxV);
                emitter.uv(1, z1, maxV);
                emitter.uv(0, z1, y0);
            }
            case UP -> {
                emitter.uv(0, x0, z1);
                emitter.uv(1, x1, z1);
                emitter.uv(2, x1, z0);
                emitter.uv(3, x0, z0);
            }
            case DOWN -> {
                emitter.uv(0, x0, z0);
                emitter.uv(1, x1, z0);
                emitter.uv(2, x1, z1);
                emitter.uv(3, x0, z1);
            }
        }
    }

    record BakeArgs(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings modelBakeSettings) {}
}
