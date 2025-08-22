package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.client.model.sprite.ChipsSprite;
import zedzee.github.io.chips.client.model.sprite.ChipsSpriteInfo;
import zedzee.github.io.chips.render.RenderData;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record ChipsModel(Function<RenderData, Map<VoxelShape, ChipsSpriteInfo>> spriteGetter) {
    public void emitQuads(QuadEmitter emitter, RenderData renderData) {
        Map<VoxelShape, ChipsSpriteInfo> spriteInfo = spriteGetter.apply(renderData);

        for (VoxelShape shape : spriteInfo.keySet()) {
            shape.forEachBox((fromX, fromY, fromZ, toX, toY, toZ) -> {
                addQuads(emitter,
                        spriteInfo.get(shape),
                        (float) fromX,
                        (float) fromY,
                        (float) fromZ,
                        (float) toX,
                        (float) toY,
                        (float) toZ
                );
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
            List<ChipsSprite> sprites = spriteInfo.getSprites(direction);

//            if (sprites.isEmpty()) {
//                emitQuad(emitter, spriteInfo.getParticleSprite(), direction, fromX, fromY, fromZ, toX, toY, toZ);
//                continue;
//            }

            sprites.forEach(sprite -> {
                if (sprite.sprite() == null) {
                    return;
                }

                emitQuad(
                        emitter,
                        sprite,
                        direction,
                        fromX,
                        fromY,
                        fromZ,
                        toX,
                        toY,
                        toZ
                );
            });
        }
    }

    private void emitQuad(
            QuadEmitter emitter,
            ChipsSprite sprite,
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

        applySprite(emitter, sprite, direction, fromX, fromY, fromZ, toX, toY, toZ);
        emit(emitter, sprite);
    }

    private void applySprite(
            QuadEmitter emitter,
            ChipsSprite sprite,
            Direction direction,
            float fromX,
            float fromY,
            float fromZ,
            float toX,
            float toY,
            float toZ
    ) {
        Sprite actualSprite = sprite.sprite();
        emitter.spriteBake(actualSprite, MutableQuadView.BAKE_LOCK_UV);

        float minU = actualSprite.getMinU();
        float maxU = actualSprite.getMaxU();

        float minV = actualSprite.getMaxV();
        float maxV = actualSprite.getMinV();

        float x0 = MathHelper.lerp(fromX, minU, maxU);
        float x1 = MathHelper.lerp(toX, minU, maxU);

        float height = Math.abs(toY - fromY);
        float y0 = MathHelper.lerp(1 - height, minV, maxV);

        float z0 = MathHelper.lerp(fromZ, minU, maxU);
        float z1 = MathHelper.lerp(toZ, minU, maxU);

        switch (direction) {
            case NORTH -> {
                emitter.uv(0, x0, maxV);
                emitter.uv(1, x1, maxV);
                emitter.uv(2, x1, y0);
                emitter.uv(3, x0, y0);
            }
            case SOUTH -> {
                emitter.uv(3, x0, maxV);
                emitter.uv(2, x1, maxV);
                emitter.uv(1, x1, y0);
                emitter.uv(0, x0, y0);
            }
            case EAST ->  {
                emitter.uv(0, z0, y0);
                emitter.uv(1, z0, maxV);
                emitter.uv(2, z1, maxV);
                emitter.uv(3, z1, y0);
            }
            case WEST -> {
                emitter.uv(3, z0, y0);
                emitter.uv(2, z0, maxV);
                emitter.uv(1, z1, maxV);
                emitter.uv(0, z1, y0);
            }
        }
    }

    private void emit(QuadEmitter emitter, ChipsSprite sprite) {
//        ChipsSpriteInfo info = spriteGetter.get();
//        if (info.spriteMap().containsKey(direction)) {
//            emitter.spriteBake(info.spriteMap().get(direction), MutableQuadView.BAKE_LOCK_UV);
//        } else {
//            emitter.spriteBake(info.particleSprite(), MutableQuadView.BAKE_LOCK_UV);
//        }
        emitter.renderLayer(BlockRenderLayer.CUTOUT_MIPPED);

//        emitter.spriteBake(sprite.sprite(), MutableQuadView.BAKE_LOCK_UV);

        if (sprite.tintIndex() != -1) {
            emitter.tintIndex(sprite.tintIndex());
            int[] colors = sprite.colors();
            emitter.color(colors[0], colors[1], colors[2], colors[3]);
        } else {
            emitter.color(-1, -1, -1, -1);
        }
        emitter.emit();
    }
}
