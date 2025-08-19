package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.Block;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import zedzee.github.io.chips.render.RenderData;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public record ChipsModel(BiFunction<RenderData, Function<Block, Integer>, Map<VoxelShape, ChipsSpriteInfo>> spriteGetter) {
    public void emitQuads(QuadEmitter emitter, RenderData renderData, Function<Block, Integer> tintGetter) {
        Map<VoxelShape, ChipsSpriteInfo> spriteInfo = spriteGetter.apply(renderData, tintGetter);

        for (VoxelShape shape : spriteInfo.keySet()) {
            shape.forEachBox((fromX, fromY, fromZ, toX, toY, toZ) -> {
                addQuads(emitter,
                        spriteInfo.get(shape),
                        (float) fromX,
                        (float) fromY,
                        (float) fromZ,
                        (float) toX,
                        (float) toY,
                        (float) toZ);
            });
        }
    }

    private void addQuads(QuadEmitter emitter,
                          ChipsSpriteInfo spriteInfo,
                          float fromX,
                          float fromY,
                          float fromZ,
                          float toX,
                          float toY,
                          float toZ
    ) {
        for (Direction direction : Direction.values()) {
            emitQuad(emitter, spriteInfo, direction, fromX, fromY, fromZ, toX, toY, toZ);
        }
    }

    private void emitQuad(
            QuadEmitter emitter,
            ChipsSpriteInfo spriteInfo,
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

        emit(emitter, spriteInfo, direction);
    }

    private void emit(QuadEmitter emitter, ChipsSpriteInfo spriteInfo, Direction direction) {
//        ChipsSpriteInfo info = spriteGetter.get();
//        if (info.spriteMap().containsKey(direction)) {
//            emitter.spriteBake(info.spriteMap().get(direction), MutableQuadView.BAKE_LOCK_UV);
//        } else {
//            emitter.spriteBake(info.particleSprite(), MutableQuadView.BAKE_LOCK_UV);
//        }
        emitter.renderLayer(BlockRenderLayer.TRANSLUCENT);

        int tint = spriteInfo.tint();
        emitter.spriteBake(spriteInfo.particleSprite(), MutableQuadView.BAKE_LOCK_UV);

        emitter.color(tint, tint, tint, tint);
        emitter.emit();
    }
}
