package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.model.StingerModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.client.model.sprite.CapturingQuadEmitter;
import zedzee.github.io.chips.client.model.sprite.ChipsSprite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class TestBlockModel implements BlockStateModel, BlockStateModel.UnbakedGrouped {
    private Sprite particleSprite;

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
    public void resolve(Resolver resolver) {

    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockRenderView blockView, BlockPos pos, BlockState state, Random random, Predicate<@Nullable Direction> cullTest) {
        BlockState grassState = Blocks.GRASS_BLOCK.getDefaultState();
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        BlockStateModel blockStateModel = blockRenderManager.getModel(grassState);
        BlockColors colors = MinecraftClient.getInstance().getBlockColors();

        Map<Direction, List<ChipsSprite>> quadMap = new HashMap<>();

        CapturingQuadEmitter capturingEmitter = new CapturingQuadEmitter();
        capturingEmitter.captureTo(quadMap);
        blockStateModel.emitQuads(capturingEmitter, blockView, pos, grassState, random, cullTest);

        quadMap.keySet().forEach(direction -> {
            List<ChipsSprite> sprites = quadMap.get(direction);

            sprites.forEach(sprite -> {
                emitter.renderLayer(BlockRenderLayer.CUTOUT_MIPPED);
                emitter.square(direction, 0, 0, 1, 1, 0);
                emitter.spriteBake(sprite.sprite(), MutableQuadView.BAKE_LOCK_UV);
                int defColor = ColorHelper.getArgb(255, 255, 255, 255);

                if (sprite.tintIndex() != -1) {
                    int color = colors.getColor(grassState, blockView, pos, sprite.tintIndex());
                    color = ColorHelper.withAlpha(255, color);
                    emitter.color(color, color, color, color);
                    emitter.tintIndex(sprite.tintIndex());
                } else {
                    emitter.color(defColor, defColor, defColor, defColor);
                }

                emitter.emit();
            });
        });
    }

    @Override
    public @Nullable Object createGeometryKey(BlockRenderView blockView, BlockPos pos, BlockState state, Random random) {
        return null;
    }

    @Override
    public Sprite particleSprite(BlockRenderView blockView, BlockPos pos, BlockState state) {
        return particleSprite;
    }
}
