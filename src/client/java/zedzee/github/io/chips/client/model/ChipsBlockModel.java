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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
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
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.component.ChipsBlockItemComponent;
import zedzee.github.io.chips.component.ChipsComponents;
import zedzee.github.io.chips.item.ChipsItems;
import zedzee.github.io.chips.render.RenderData;
import zedzee.github.io.chips.util.RandomSupplier;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

// i swear ill change this name
public class ChipsBlockModel implements UnbakedModel, BakedModel, FabricBakedModel {
    private static final SpriteIdentifier FALLBACK_PARTICLE_SPRITE = new SpriteIdentifier(
            PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("block/dirt")
    );

    private RandomSupplier<Sprite> particleSpriteSupplier;
    private Sprite fallbackParticleSprite;
    private RenderMaterial renderMaterialNormal;
    private RenderMaterial renderMaterialTranslucent;
    private BlockColors blockColors;

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
        if (!stack.isOf(ChipsItems.CHIPS_BLOCK_ITEM) || !stack.contains(ChipsComponents.BLOCK_COMPONENT_COMPONENT)) {
            return;
        }

        ChipsBlockItemComponent blockItemComponent = stack.get(ChipsComponents.BLOCK_COMPONENT_COMPONENT);
        assert blockItemComponent != null;
        ChipsItemRenderData renderData = new ChipsItemRenderData(blockItemComponent.block());
        emitModelQuads(context.getEmitter(), randomSupplier.get(), renderData, (state, tintIndex) ->
                this.blockColors.getColor(state, MinecraftClient.getInstance().world, BlockPos.ORIGIN, tintIndex)
        );
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
        emitModelQuads(
                context.getEmitter(),
                randomSupplier.get(),
                renderData,
                (blockState, tintIndex) -> this.blockColors.getColor(
                        blockState, blockView, pos, tintIndex
                )
        );
    }

    private void emitModelQuads(
            QuadEmitter emitter,
            Random random,
            RenderData renderData,
            BlockColorProvider blockColorProvider
    ) {
        // janky but whatever
        boolean addSprites;
        if (this.particleSpriteSupplier == null) {
            this.particleSpriteSupplier = new RandomSupplier<>(random, new ArrayList<>());
            addSprites = true;
        } else {
            addSprites = false;
        }

        final Set<Block> blocks = renderData.getBlocks();
        blocks.forEach(block -> {
            MinecraftClient client = MinecraftClient.getInstance();
            BlockState state = block.getDefaultState();

            BakedModel blockModel =
                    client.getBlockRenderManager().getModel(state);

            if (blockModel == null || blockModel == client.getBakedModelManager().getMissingModel()) {
                return;
            }

            final int chips = renderData.getChips(block);
            final VoxelShape shape = ChipsBlock.getShape(chips);

            final BlockState defaultState = block.getDefaultState();
            shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
                for (Direction direction : Direction.values()) {
                    List<BakedQuad> quads = blockModel.getQuads(defaultState, direction, random);
                    for (BakedQuad quad : quads) {
                        if (addSprites) {
                            this.particleSpriteSupplier.add(quad.getSprite());
                        }

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
                        RenderLayer layer = RenderLayers.getBlockLayer(defaultState);
                        emitter.material(layer.isTranslucent() ? renderMaterialTranslucent : renderMaterialNormal);
                        emitter.cullFace(null);

                        if (quad.hasColor()) {
                            int color = blockColorProvider.getColor(defaultState, quad.getColorIndex());
                            color = ColorHelper.Argb.withAlpha(0xFF, color);

                            emitter.colorIndex(quad.getColorIndex());
                            emitter.color(color, color, color, color);
                        } else {
                            emitter.color(-1, -1, -1, -1);
                        }
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
        this.fallbackParticleSprite = textureGetter.apply(FALLBACK_PARTICLE_SPRITE);

        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        MaterialFinder finder = renderer.materialFinder();
        this.renderMaterialNormal = finder
                .ambientOcclusion(TriState.TRUE)
                .disableDiffuse(false)
                .blendMode(BlendMode.CUTOUT_MIPPED)
                .find();
        this.renderMaterialTranslucent = finder
                .ambientOcclusion(TriState.TRUE)
                .disableDiffuse(false)
                .blendMode(BlendMode.TRANSLUCENT)
                .find();

        MinecraftClient client = MinecraftClient.getInstance();
        this.blockColors = client.getBlockColors();

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

        final float minU = sprite.getMinU();
        final float maxU = sprite.getMaxU();

        final float minV = sprite.getMinV();
        final float maxV = sprite.getMaxV();

        // can just use fromX and toX because they will both be within 0-1 (same applies for z)
        final float startXU = MathHelper.lerp(fromX, minU, maxU);
        final float stopXU = MathHelper.lerp(toX, minU, maxU);

        final float startYV = minV;
        final float height = Math.abs(toY - fromY);
        final float stopYV = MathHelper.lerp(height, minV, maxV);

        final float startYU = MathHelper.lerp(fromY, minU, maxU);
        final float stopYU = MathHelper.lerp(toY, minU, maxU);

        final float startZU = MathHelper.lerp(fromZ, minU, maxU);
        final float stopZU = MathHelper.lerp(toZ, minU, maxU);

        final float startZV = MathHelper.lerp(fromZ, minV, maxV);
        final float stopZV = MathHelper.lerp(toZ, minV, maxV);

        switch (direction) {
            case NORTH -> {
                emitter.uv(0, startXU, stopYV);
                emitter.uv(1, stopXU, stopYV);
                emitter.uv(2, stopXU, startYV);
                emitter.uv(3, startXU, startYV);
            }
            case SOUTH -> {
                emitter.uv(3, startXU, stopYV);
                emitter.uv(2, stopXU, stopYV);
                emitter.uv(1, stopXU, startYV);
                emitter.uv(0, startXU, startYV);
            }
            case EAST ->  {
                emitter.uv(0, startYU, startZV);
                emitter.uv(1, stopYU, startZV);
                emitter.uv(2, stopYU, stopZV);
                emitter.uv(3, startYU, stopZV);
            }
            case WEST -> {
                emitter.uv(3, startYU, startZV);
                emitter.uv(2, stopYU, startZV);
                emitter.uv(1, stopYU, stopZV);
                emitter.uv(0, startYU, stopZV);
            }
            case UP -> {
                emitter.uv(0, startXU, startZV);
                emitter.uv(1, stopXU, startZV);
                emitter.uv(2, stopXU, stopZV);
                emitter.uv(3, startXU, stopZV);
            }
            case DOWN -> {
                emitter.uv(3, startXU, startZV);
                emitter.uv(2, stopXU, startZV);
                emitter.uv(1, stopXU, stopZV);
                emitter.uv(0, startXU, stopZV);
            }
        }
    }
}
