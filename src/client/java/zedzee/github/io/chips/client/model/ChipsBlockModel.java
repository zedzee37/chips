package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.model.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.client.model.sprite.ChipsSprite;
import zedzee.github.io.chips.client.model.sprite.ChipsSpriteInfo;
import zedzee.github.io.chips.client.model.sprite.SpriteCapturingQuadEmitter;
import zedzee.github.io.chips.render.RenderData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class ChipsBlockModel implements BlockStateModel, BlockStateModel.UnbakedGrouped {
    private Sprite particleSprite;
    private ChipsModel model;

    private final SpriteCapturingQuadEmitter capturingEmitter;

    public ChipsBlockModel() {
        capturingEmitter = new SpriteCapturingQuadEmitter();
    }

    @Override
    public void emitQuads(QuadEmitter emitter,
                          BlockRenderView blockView,
                          BlockPos pos,
                          BlockState state,
                          Random random,
                          Predicate<@Nullable Direction> cullTest
    ) {
        if (!state.isOf(ChipsBlocks.CHIPS_BLOCK)) {
            return;
        }

        Object renderData = blockView.getBlockEntityRenderData(pos);
        if (!(renderData instanceof RenderData chipsRenderData)) {
            return;
        }

        model.emitQuads(emitter, chipsRenderData, block -> {
            int tint = ColorHelper.getArgb(255, 255, 255, 255);

            BlockState defaultState = block.getDefaultState();
            if (defaultState.isIn(BlockTags.LEAVES)) {
                 tint = BiomeColors.getFoliageColor(blockView, pos);
            }

            tint = ColorHelper.withAlpha(255, tint);

            return tint;
        });
    }

    @Override
    public void resolve(Resolver resolver) {
    }

    @Override
    public void addParts(Random random, List<BlockModelPart> parts) {}

    @Override
    public Sprite particleSprite() {
        return particleSprite;
    }

    @Override
    public BlockStateModel bake(BlockState state, Baker baker) {
//        ErrorCollectingSpriteGetter spriteGetter = baker.getSpriteGetter();
//
        BiFunction<RenderData, Function<Block, Integer>, Map<VoxelShape, ChipsSpriteInfo>> spriteGetter =
                (blockEntity, tintGetter) -> {
            HashMap<VoxelShape, ChipsSpriteInfo> spriteInfo = new HashMap<>();

            blockEntity.forEachKey(block -> {
                // TODO: use this to get the proper sprites for each side:

                /*
                BlockStateModel blockStateModel = MinecraftClient
                        .getInstance()
                        .getBlockRenderManager()
                        .getModel(Blocks.DIAMOND_BLOCK.getDefaultState());

                blockStateModel.emitQuads(capturingEmitter);

                MinecraftClient.getInstance().getBlockColors()
                 */


                Identifier identifier = Registries.BLOCK.getId(block);
                identifier = Identifier.of(identifier.getNamespace(), "block/" + identifier.getPath());

                BakedSimpleModel model = baker.getModel(identifier);
                Sprite partSprite = model.getParticleTexture(model.getTextures(), baker);
                HashMap<Direction, List<ChipsSprite>> spriteMap = new HashMap<>();

                ChipsSpriteInfo chipsSpriteInfo = new ChipsSpriteInfo(
                        new ChipsSprite(partSprite, tintGetter.apply(block)),
                        spriteMap
                );

                if (particleSprite == null) {
                    particleSprite = chipsSpriteInfo.getParticleSprite().sprite();
                }

                spriteInfo.put(
                        ChipsBlock.getShape(blockEntity.getChips(block)),
                        chipsSpriteInfo
                );
            });

            return spriteInfo;
        };
        this.model = new ChipsModel(spriteGetter);

//        final SpriteIdentifier spriteIdentifier = new SpriteIdentifier(S.BLOCK_ATLAS_TEXTURE, identifier);
//        this.sprite = spriteGetter.get(spriteIdentifier, () -> "");

        return this;
    }

    @Override
    public Object getEqualityGroup(BlockState state) {
        return null;
    }
}