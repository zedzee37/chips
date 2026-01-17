package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
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
    private Supplier<BakeArgs> bakeArgsSupplier;
    private RandomSupplier<Sprite> particleSpriteSupplier;

    // for versions before 1.21, replace `Identifier.ofVanilla` with `new Identifier`.
    private static final SpriteIdentifier[] SPRITE_IDS = new SpriteIdentifier[]{
            new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("block/furnace_front_on")),
            new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("block/furnace_top"))
    };
    private final Sprite[] sprites = new Sprite[SPRITE_IDS.length];

    // Some constants to avoid magic numbers, these need to match the SPRITE_IDS
    private static final int SPRITE_SIDE = 0;
    private static final int SPRITE_TOP = 1;
    private Mesh mesh;

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
            return sprites[SPRITE_TOP];
        }

        Sprite randomSprite = particleSpriteSupplier.get();
        return randomSprite == null ? sprites[SPRITE_TOP] : randomSprite;
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
        mesh.outputTo(context.getEmitter());
//        if (!stack.isOf(ChipsItems.CHIPS_BLOCK_ITEM) || stack.contains(ChipsComponents.BLOCK_COMPONENT_COMPONENT)) {
//            return;
//        }
//
//        ChipsBlockItemComponent blockItemComponent = stack.get(ChipsComponents.BLOCK_COMPONENT_COMPONENT);
//        assert blockItemComponent != null;
//        ChipsItemRenderData renderData = new ChipsItemRenderData(blockItemComponent.block());
//        emitModelQuads(context.getEmitter(), renderData);
    }

    @Override
    public void emitBlockQuads(
            BlockRenderView blockView,
            BlockState state,
            BlockPos pos,
            Supplier<Random> randomSupplier,
            RenderContext context
    ) {
        mesh.outputTo(context.getEmitter());
//        Object object = blockView.getBlockEntityRenderData(pos);
//
//        if (object != null && !(object instanceof RenderData)) {
//            return;
//        }
//
//        RenderData renderData = (RenderData)object;
//        assert renderData != null;
//        emitModelQuads(context.getEmitter(), renderData);
    }

    private void emitModelQuads(QuadEmitter emitter, RenderData renderData) {
//        BakeArgs bakeArgs = bakeArgsSupplier.get();
//
//        Set<Block> blocks = renderData.getBlocks();
//        blocks.forEach(block -> {
//            UnbakedModel model = bakeArgs.baker().getOrLoadModel(Registries.BLOCK.getId(block));
//            BakedModel blockModel = model.bake(bakeArgs.baker(), bakeArgs.textureGetter(), bakeArgs.modelBakeSettings());
//
//            if (blockModel == null) {
//                return;
//            }
//
//            int chips = renderData.getChips(block);
//            VoxelShape shape = ChipsBlock.getShape(chips);
//
//            for (Direction direction : Direction.values()) {
//            }
//        });

        for (Direction direction : Direction.values()) {
            int spriteIdx = direction == Direction.UP || direction == Direction.DOWN ? SPRITE_TOP : SPRITE_SIDE;
            // Add a new face to the mesh
            emitter.square(direction, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f);
            // Set the sprite of the face, must be called after .square()
            // We haven't specified any UV coordinates, so we want to use the whole texture. BAKE_LOCK_UV does exactly that.
            emitter.spriteBake(sprites[spriteIdx], MutableQuadView.BAKE_LOCK_UV);
            // Enable texture usage
            emitter.color(-1, -1, -1, -1);
            // Add the quad to the mesh
            emitter.emit();
        }
    }

    @Override
    public @Nullable BakedModel bake(
            Baker baker,
            Function<SpriteIdentifier, Sprite> textureGetter,
            ModelBakeSettings rotationContainer
    ) {

        for(int i = 0; i < SPRITE_IDS.length; ++i) {
            sprites[i] = textureGetter.apply(SPRITE_IDS[i]);
        }
        // Build the mesh using the Renderer API
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        MeshBuilder builder = renderer.meshBuilder();
        QuadEmitter emitter = builder.getEmitter();

        for(Direction direction : Direction.values()) {
            // UP and DOWN share the Y axis
            int spriteIdx = direction == Direction.UP || direction == Direction.DOWN ? SPRITE_TOP : SPRITE_SIDE;
            // Add a new face to the mesh
            emitter.square(direction, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f);
            // Set the sprite of the face, must be called after .square()
            // We haven't specified any UV coordinates, so we want to use the whole texture. BAKE_LOCK_UV does exactly that.
            emitter.spriteBake(sprites[spriteIdx], MutableQuadView.BAKE_LOCK_UV);
            // Enable texture usage
            emitter.color(-1, -1, -1, -1);
            // Add the quad to the mesh
            emitter.emit();
        }
        mesh = builder.build();

        this.bakeArgsSupplier = () -> new BakeArgs(baker, textureGetter, rotationContainer);
        return this;
    }

    record BakeArgs(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings modelBakeSettings) {}
}
