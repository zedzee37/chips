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
        for (Direction direction : Direction.values()) {
            emitQuad(emitter, direction, fromX, fromY, fromZ, toX, toY, toZ);
        }
    }

    // not using square because it emits with an offset
    private void emitQuad(
            QuadEmitter emitter,
            Direction direction,
            float fromX,
            float fromY,
            float fromZ,
            float toX,
            float toY,
            float toZ
    ) {
        emitter.nominalFace(direction);

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
                emitter.pos(0, fromX, fromY, toZ);
                emitter.pos(1, toX, fromY, toZ);
                emitter.pos(2, toX, toY, toZ);
                emitter.pos(3, fromX, toY, toZ);
                break;
            case SOUTH:
                emitter.pos(3, fromX, fromY, fromZ);
                emitter.pos(2, toX, fromY, fromZ);
                emitter.pos(1, toX, toY, fromZ);
                emitter.pos(0, fromX, toY, fromZ);
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
    public void addParts(Random random, List<BlockModelPart> parts) {}

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
