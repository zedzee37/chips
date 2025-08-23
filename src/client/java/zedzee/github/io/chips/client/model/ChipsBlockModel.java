package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.client.model.sprite.ChipsSprite;
import zedzee.github.io.chips.client.model.sprite.ChipsSpriteInfo;
import zedzee.github.io.chips.render.RenderData;

import java.util.*;
import java.util.function.Predicate;

// i swear ill change this name
public class ChipsBlockModel implements BlockStateModel, BlockStateModel.UnbakedGrouped {
    private Sprite particleSprite;

    // should always be left blank
    @Override
    public void addParts(Random random, List<BlockModelPart> parts) {}

    @Override
    public Sprite particleSprite() {
        return particleSprite;
    }

    @Override
    public BlockStateModel bake(BlockState state, Baker baker) {
        return this;
    }

    @Override
    public Object getEqualityGroup(BlockState state) {
        return null;
    }

    @Override
    public void resolve(Resolver resolver) {}

    @Override
    public void emitQuads(QuadEmitter emitter, BlockRenderView blockView, BlockPos pos, BlockState state, Random random, Predicate<@Nullable Direction> cullTest) {
        Object objRenderData = blockView.getBlockEntityRenderData(pos);
        if (!(objRenderData instanceof RenderData chipsRenderData)) {
            return;
        }

        ChipsModel model = new ChipsModel(renderData -> {
            Map<VoxelShape, ChipsSpriteInfo> spriteInfoMap = new HashMap<>();
            renderData.forEachKey(block -> {
                int chips = renderData.getChips(block);
                VoxelShape shape = ChipsBlock.getShape(chips);

                BlockStateModelHelper modelHelper = new BlockStateModelHelper(block);

                Map<Direction, List<ChipsSprite>> sprites = modelHelper.getSprites(blockView, pos, random);
                this.particleSprite = modelHelper.getParticleSprite();

                spriteInfoMap.put(shape, new ChipsSpriteInfo(
                        new ChipsSprite(particleSprite),
                        sprites,
                        renderData.shouldUseDefaultUv(block))
                );
            });

            return spriteInfoMap;
        });

        model.emitQuads(emitter, chipsRenderData);
    }

    @Override
    public @Nullable Object createGeometryKey(BlockRenderView blockView, BlockPos pos, BlockState state, Random random) {
        Object renderData = blockView.getBlockEntityRenderData(pos);
        if (!(renderData instanceof RenderData chipsRenderData)) {
            return null;
        }
        return new GeometryKey(chipsRenderData);
    }

    @Override
    public Sprite particleSprite(BlockRenderView blockView, BlockPos pos, BlockState state) {
        Object objRenderData = blockView.getBlockEntityRenderData(pos);
        if (!(objRenderData instanceof RenderData chipsRenderData)) {
            return particleSprite;
        }

        Random random = Random.create();

        List<Sprite> particleSprites = new ArrayList<>();
        chipsRenderData.forEachKey(block -> {
            BlockStateModel model = MinecraftClient.getInstance().getBlockRenderManager().getModel(block.getDefaultState());
            particleSprites.add(model.particleSprite(blockView, pos, state));
        });

        if (particleSprites.isEmpty()) {
            return particleSprite;
        }

        int i = random.nextInt(particleSprites.size());
        return particleSprites.get(i);
    }

    private record GeometryKey(RenderData renderData) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof GeometryKey(RenderData data))) {
                return false;
            }

            return data.equals(renderData);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(renderData);
        }
    }
}
