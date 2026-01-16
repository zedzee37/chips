package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
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

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

// i swear ill change this name
public class ChipsBlockModel implements UnbakedModel, BakedModel, FabricBakedModel {
    private Sprite particleSprite;
    private Supplier<BakeArgs> bakeArgsSupplier;

//    @Override
//    public void emitQuads(QuadEmitter emitter,
//                          BlockRenderView blockView,
//                          BlockPos pos,
//                          BlockState state,
//                          Random random,
//                          Predicate<@Nullable Direction> cullTest) {
//        Object objRenderData = blockView.getBlockEntityRenderData(pos);
//        if (!(objRenderData instanceof RenderData chipsRenderData)) {
//            return;
//        }
//
//        ChipsModel model = new ChipsModel(renderData -> {
//            Map<VoxelShape, ChipsSpriteInfo> spriteInfoMap = new HashMap<>();
//            renderData.forEachBlock(block -> {
//                int chips = renderData.getChips(block);
//                VoxelShape shape = ChipsBlock.getShape(chips);
//
//                BlockStateModelHelper modelHelper = new BlockStateModelHelper(block);
//
//                Map<Direction, List<ChipsSprite>> sprites = modelHelper.getSprites(blockView, pos, random);
//                this.particleSprite = modelHelper.getParticleSprite();
//
//                spriteInfoMap.put(shape, new ChipsSpriteInfo(
//                        new ChipsSprite(particleSprite),
//                        sprites,
//                        renderData.shouldUseDefaultUv(block))
//                );
//            });
//
//            return spriteInfoMap;
//        });
//
//        model.emitQuads(emitter, chipsRenderData);
//    }
//
//    @Override
//    public @Nullable Object createGeometryKey(BlockRenderView blockView, BlockPos pos, BlockState state, Random random) {
//        Object renderData = blockView.getBlockEntityRenderData(pos);
//        if (!(renderData instanceof RenderData chipsRenderData)) {
//            return null;
//        }
//        return new GeometryKey(chipsRenderData);
//    }
//
//    @Override
//    public Sprite particleSprite(BlockRenderView blockView, BlockPos pos, BlockState state) {
//        Object objRenderData = blockView.getBlockEntityRenderData(pos);
//        if (!(objRenderData instanceof RenderData chipsRenderData)) {
//            return particleSprite;
//        }
//
//        Random random = Random.create();
//
//        List<Sprite> particleSprites = new ArrayList<>();
//        chipsRenderData.forEachBlock(block -> {
//            BlockStateModel model = MinecraftClient.getInstance().getBlockRenderManager().getModel(block.getDefaultState());
//            particleSprites.add(model.particleSprite(blockView, pos, state));
//        });
//
//        if (particleSprites.isEmpty()) {
//            return particleSprite;
//        }
//
//        int i = random.nextInt(particleSprites.size());
//        return particleSprites.get(i);
//    }
//
//
//    private record GeometryKey(RenderData renderData) {
//        @Override
//        public boolean equals(Object o) {
//            if (!(o instanceof GeometryKey(RenderData data))) {
//                return false;
//            }
//
//            return data.equals(renderData);
//        }
//
//        @Override
//        public int hashCode() {
//            return Objects.hashCode(renderData);
//        }
//    }


    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return List.of();
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
        return null;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return null;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return List.of();
    }

    @Override
    public void setParents(Function<Identifier, UnbakedModel> modelLoader) {

    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        if (!stack.isOf(ChipsItems.CHIPS_BLOCK_ITEM) || stack.contains(ChipsComponents.BLOCK_COMPONENT_COMPONENT)) {
            return;
        }

        ChipsBlockItemComponent blockItemComponent = stack.get(ChipsComponents.BLOCK_COMPONENT_COMPONENT);
        assert blockItemComponent != null;
        ChipsItemRenderData renderData = new ChipsItemRenderData(blockItemComponent.block());
        emitModelQuads(renderData);
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
        emitModelQuads(renderData);
    }

    private void emitModelQuads(RenderData renderData) {
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        assert renderer != null;
        MeshBuilder builder = renderer.meshBuilder();
        QuadEmitter emitter = builder.getEmitter();
        BakeArgs bakeArgs = bakeArgsSupplier.get();

        renderData.forEachBlock(block -> {
            UnbakedModel model = bakeArgs.baker().getOrLoadModel(Registries.BLOCK.getId(block));
            BakedModel blockModel = model.bake(bakeArgs.baker(), bakeArgs.textureGetter(), bakeArgs.modelBakeSettings());

            if (blockModel == null) {
                return;
            }

            for (Direction direction : Direction.values()) {

            }
        });
    }

    @Override
    public @Nullable BakedModel bake(
            Baker baker,
            Function<SpriteIdentifier, Sprite> textureGetter,
            ModelBakeSettings rotationContainer
    ) {
        this.bakeArgsSupplier = () -> new BakeArgs(baker, textureGetter, rotationContainer);
        return this;
    }

    record BakeArgs(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings modelBakeSettings) {}
}
