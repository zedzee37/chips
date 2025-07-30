package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.ChipsBlock;

import java.util.List;
import java.util.function.Predicate;

public class ChipsBlockModel implements UnbakedModel, BakedModel, FabricBakedModel {
//    @Override
//    public void emitQuads(QuadEmitter emitter, BlockRenderView blockView, BlockPos pos, BlockState state, Random random, Predicate<@Nullable Direction> cullTest) {
//        Chips.LOGGER.info("gug");
//        if (!state.contains(ChipsBlock.CHIPS_PROPERTY)) {
//            return;
//        }
//        Chips.LOGGER.info("bujg");
//
//        int shapeIdx = state.get(ChipsBlock.CHIPS_PROPERTY);
//        VoxelShape shape = ChipsBlock.SHAPES[shapeIdx];
//        shape.forEachBox((fromX, fromY, fromZ, toX, toY, toZ) -> {
//            addQuads(emitter, (float) fromX, (float) fromY, (float) fromZ, (float) toX, (float) toY, (float) toZ);
//        });
//    }
//
//    private void addQuads(QuadEmitter emitter, float fromX, float fromY, float fromZ, float toX, float toY, float toZ) {
//        emitter.square(Direction.DOWN, fromX, fromZ, toX, toZ, fromY);
//        emitter.emit();
//
//        emitter.square(Direction.UP, fromX, fromZ, toX, toZ, toY);
//        emitter.emit();
//
//        emitter.square(Direction.NORTH, fromX, fromY, toX, toY, fromZ);
//        emitter.emit();
//
//        emitter.square(Direction.SOUTH, fromX, fromY, toX, toY, toZ);
//        emitter.emit();
//
//        emitter.square(Direction.WEST, fromZ, fromY, toZ, toY, fromX);
//        emitter.emit();
//
//        emitter.square(Direction.EAST, fromZ, fromY, toZ, toY, toX);
//        emitter.emit();
//    }

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
    public boolean isSideLit() {
        return false;
    }

    @Override
    public Sprite getParticleSprite() {
        return null;
    }

    @Override
    public BakedModel bake(ModelTextures textures, Baker baker, ModelBakeSettings settings, boolean ambientOcclusion, boolean isSideLit, ModelTransformation transformation) {
        return null;
    }

    @Override
    public void resolve(Resolver resolver) {

    }
}
