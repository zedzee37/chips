package zedzee.github.io.chips.client.model;

import com.google.common.base.Suppliers;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.util.ChipsBlockHelpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ChipsBlockModel implements BlockStateModel, BlockStateModel.UnbakedGrouped {
    private static final Identifier TEMP_BLOCK_TEXTURE = Identifier.ofVanilla("block/spruce_planks");
    private Supplier<SpriteInfo> spriteSupplier;

    @Override
    public void emitQuads(QuadEmitter emitter, BlockRenderView blockView, BlockPos pos, BlockState state, Random random, Predicate<@Nullable Direction> cullTest) {
        if (!state.contains(ChipsBlockHelpers.CHIPS)) {
            return;
        }

        VoxelShape shape = ChipsBlockHelpers.getOutlineShape(state);
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

        emit(emitter, direction);
    }

    private void emit(QuadEmitter emitter, Direction direction) {
        SpriteInfo info = spriteSupplier.get();
        if (info.spriteMap().containsKey(direction)) {
            emitter.spriteBake(info.spriteMap().get(direction), MutableQuadView.BAKE_LOCK_UV);
        } else {
            emitter.spriteBake(info.particleSprite(), MutableQuadView.BAKE_LOCK_UV);
        }

        emitter.color(-1, -1, -1, -1);
        emitter.emit();
    }

    @Override
    public void resolve(Resolver resolver) {
    }

    @Override
    public void addParts(Random random, List<BlockModelPart> parts) {}

    @Override
    public Sprite particleSprite() {
        return spriteSupplier.get().particleSprite();
    }

    @Override
    public BlockStateModel bake(BlockState state, Baker baker) {
//        ErrorCollectingSpriteGetter spriteGetter = baker.getSpriteGetter();
//
        spriteSupplier = Suppliers.memoize(() -> {
            Block block = state.getBlock();
            Identifier identifier = Registries.BLOCK.getId(block);
            identifier = Identifier.of(identifier.getNamespace(), "block/" + identifier.getPath());

            BakedSimpleModel model = baker.getModel(identifier);
            Sprite particleSprite = model.getParticleTexture(model.getTextures(), baker);
            HashMap<Direction, Sprite> spriteMap = new HashMap<>();
            return new SpriteInfo(particleSprite, spriteMap);
        });

//        final SpriteIdentifier spriteIdentifier = new SpriteIdentifier(S.BLOCK_ATLAS_TEXTURE, identifier);
//        this.sprite = spriteGetter.get(spriteIdentifier, () -> "");

        return this;
    }

    @Override
    public Object getEqualityGroup(BlockState state) {
        return null;
    }

    private record SpriteInfo(Sprite particleSprite, Map<Direction, Sprite> spriteMap) {}
}