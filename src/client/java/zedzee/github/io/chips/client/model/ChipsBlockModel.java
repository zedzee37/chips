package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.*;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.TriState;
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
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.block.CornerInfo;
import zedzee.github.io.chips.component.ChipsBlockItemComponent;
import zedzee.github.io.chips.component.ChipsComponents;
import zedzee.github.io.chips.item.ChipsItems;
import zedzee.github.io.chips.render.RenderData;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

// i swear ill change this name
public class ChipsBlockModel implements UnbakedModel, BakedModel, FabricBakedModel {
    private static final SpriteIdentifier FALLBACK_PARTICLE_SPRITE = new SpriteIdentifier(
            PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("block/dirt")
    );

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
        return this.fallbackParticleSprite;
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
    public @Nullable BakedModel bake(
            Baker baker,
            Function<SpriteIdentifier, Sprite> textureGetter,
            ModelBakeSettings rotationContainer
    ) {
        this.fallbackParticleSprite = textureGetter.apply(FALLBACK_PARTICLE_SPRITE);

        final Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        if (renderer == null) {
            return null;
        }

        final MaterialFinder finder = renderer.materialFinder();
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

        final MinecraftClient client = MinecraftClient.getInstance();
        this.blockColors = client.getBlockColors();

        return this;
    }

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
        final Object object = blockView.getBlockEntityRenderData(pos);

        if (object != null && !(object instanceof RenderData)) {
            return;
        }

        final RenderData renderData = (RenderData)object;
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
            BlockColorProvider blockColorProvider) {
        final Set<BlockState> states = renderData.getStates();
        states.forEach(state -> {
            final MinecraftClient client = MinecraftClient.getInstance();

            final BakedModel blockModel =
                    client.getBlockRenderManager().getModel(state);

            if (blockModel == null || blockModel == client.getBakedModelManager().getMissingModel()) {
                return;
            }

            final CornerInfo chips = renderData.getChips(state);
            final VoxelShape shape = ChipsBlock.getShape(chips.shape());

            shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
                for (Direction direction : Direction.values()) {
                    List<BakedQuad> quads = blockModel.getQuads(state, direction, random);
                    for (BakedQuad quad : quads) {
                        fallbackParticleSprite = quad.getSprite();

                        emitter.nominalFace(direction);
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
                                renderData.shouldUseDefaultUv(state)
                        );
                        RenderLayer layer = RenderLayers.getBlockLayer(state);
                        emitter.material(layer.isTranslucent() ? renderMaterialTranslucent : renderMaterialNormal);
                        emitter.cullFace(null);

                        if (quad.hasColor()) {
                            int color = blockColorProvider.getColor(state, quad.getColorIndex());
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

    public static void emitQuadPositions(
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
            case UP -> {
                emitter.pos(3, fromX, toY, fromZ);
                emitter.pos(2, toX, toY, fromZ);
                emitter.pos(1, toX, toY, toZ);
                emitter.pos(0, fromX, toY, toZ);
            }
            case DOWN -> {
                emitter.pos(0, fromX, fromY, fromZ);
                emitter.pos(1, toX, fromY, fromZ);
                emitter.pos(2, toX, fromY, toZ);
                emitter.pos(3, fromX, fromY, toZ);
            }
            case NORTH -> {
                emitter.pos(0, toX, toY, fromZ);
                emitter.pos(1, toX, fromY, fromZ);
                emitter.pos(2, fromX, fromY, fromZ);
                emitter.pos(3, fromX, toY, fromZ);
            }
            case SOUTH -> {
                emitter.pos(3, toX, toY, toZ);
                emitter.pos(2, toX, fromY, toZ);
                emitter.pos(1, fromX, fromY, toZ);
                emitter.pos(0, fromX, toY, toZ);
            }
            case EAST -> {
                emitter.pos(0, toX, toY, toZ);
                emitter.pos(1, toX, fromY, toZ);
                emitter.pos(2, toX, fromY, fromZ);
                emitter.pos(3, toX, toY, fromZ);
            }
            case WEST -> {
                emitter.pos(3, fromX, toY, toZ);
                emitter.pos(2, fromX, fromY, toZ);
                emitter.pos(1, fromX, fromY, fromZ);
                emitter.pos(0, fromX, toY, fromZ);
            }
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
        // reminder for my dumb ass
        // V goes top of the texture -> bottom of the texture, U goes left to right

        final float minU = sprite.getMinU();
        final float maxU = sprite.getMaxU();

        final float minV = sprite.getMinV();
        final float maxV = sprite.getMaxV();

        final float minXU = MathHelper.lerp(fromX, minU, maxU);
        final float maxXU = MathHelper.lerp(toX, minU, maxU);

        float minYV = MathHelper.lerp(fromY, maxV, minV);
        float maxYV = MathHelper.lerp(toY, maxV, minV);
        if (!defaultUv) {
            minYV = MathHelper.lerp(Math.abs(toY - fromY), minV, maxV);
            maxYV = minV;
        }

        final float minZV = MathHelper.lerp(fromZ, minV, maxV);
        final float maxZV = MathHelper.lerp(toZ, minV, maxV);

        final float minZU = MathHelper.lerp(fromZ, minU, maxU);
        final float maxZU = MathHelper.lerp(toZ, minU, maxU);

        switch (direction) {
            case UP -> {
                emitter.uv(3, minXU, minZV);
                emitter.uv(2, maxXU, minZV);
                emitter.uv(1, maxXU, maxZV);
                emitter.uv(0, minXU, maxZV);
            }
            case DOWN -> {
                emitter.uv(0, minXU, minZV);
                emitter.uv(1, maxXU, minZV);
                emitter.uv(2, maxXU, maxZV);
                emitter.uv(3, minXU, maxZV);
            }
            case NORTH -> {
                emitter.uv(0, maxXU, maxYV);
                emitter.uv(1, maxXU, minYV);
                emitter.uv(2, minXU, minYV);
                emitter.uv(3, minXU, maxYV);
            }
            case SOUTH -> {
                emitter.uv(3, maxXU, maxYV);
                emitter.uv(2, maxXU, minYV);
                emitter.uv(1, minXU, minYV);
                emitter.uv(0, minXU, maxYV);
            }
            case EAST -> {
                emitter.uv(0, maxZU, maxYV);
                emitter.uv(1, maxZU, minYV);
                emitter.uv(2, minZU, minYV);
                emitter.uv(3, minZU, maxYV);
            }
            case WEST -> {
                emitter.uv(3, maxZU, maxYV);
                emitter.uv(2, maxZU, minYV);
                emitter.uv(1, minZU, minYV);
                emitter.uv(0, minZU, maxYV);
           }
        }
    }
}
