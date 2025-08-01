package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.block.ChipsBlock;

import java.util.List;
import java.util.function.Predicate;

public class ChipsBlockModel implements BlockStateModel, BlockStateModel.UnbakedGrouped {
    private Sprite sprite;

    @Override
    public void emitQuads(QuadEmitter emitter, BlockRenderView blockView, BlockPos pos, BlockState state, Random random, Predicate<@Nullable Direction> cullTest) {
        if (!state.contains(ChipsBlock.CHIPS_PROPERTY)) {
            return;
        }

        int shapeIdx = state.get(ChipsBlock.CHIPS_PROPERTY);
        VoxelShape shape = ChipsBlock.SHAPES[shapeIdx];
        shape.forEachBox((fromX, fromY, fromZ, toX, toY, toZ) -> {
            addQuads(emitter, (float) fromX, (float) fromY, (float) fromZ, (float) toX, (float) toY, (float) toZ);
        });
    }

    private void addQuads(QuadEmitter emitter, float fromX, float fromY, float fromZ, float toX, float toY, float toZ) {
        emitter.square(Direction.DOWN, fromX, fromZ, toX, toZ, fromY);
        emit(emitter);

        emitter.square(Direction.UP, fromX, fromZ + 0.5f, toX, toZ + 0.5f, toY);
        emit(emitter);

        emitter.square(Direction.NORTH, fromX + 0.5f, fromY, toX + 0.5f, toY, fromZ);
        emit(emitter);

        emitter.square(Direction.SOUTH, fromX, fromY, toX, toY, toZ);
        emit(emitter);

        emitter.square(Direction.WEST, fromZ, fromY, toZ, toY, fromX);
        emit(emitter);

        emitter.square(Direction.EAST, fromZ + 0.5f, fromY, toZ + 0.5f, toY, toX);
        emit(emitter);
    }

    private void emit(QuadEmitter emitter) {
        emitter.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV);
        emitter.color(-1, -1, -1, -1);
        emitter.emit();
    }

    @Override
    public void resolve(Resolver resolver) {
        resolver.markDependency(Identifier.ofVanilla("block/diamond_block"));
    }

    @Override
    public void addParts(Random random, List<BlockModelPart> parts) {

    }

    @Override
    public Sprite particleSprite() {
        return sprite;
    }

    @Override
    public BlockStateModel bake(BlockState state, Baker baker) {
        ErrorCollectingSpriteGetter spriteGetter = baker.getSpriteGetter();

        final SpriteIdentifier spriteIdentifier = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("block/diamond_block"));
        this.sprite = spriteGetter.get(spriteIdentifier, () -> "");

        return this;
    }

    @Override
    public Object getEqualityGroup(BlockState state) {
        return null;
    }
}
