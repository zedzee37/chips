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
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ChipsBlockModel implements BlockStateModel, BlockStateModel.UnbakedGrouped {
    private static final Identifier TEMP_BLOCK_TEXTURE = Identifier.ofVanilla("block/spruce_planks");
    private Supplier<ChipsSpriteInfo> spriteSupplier;
    private ChipsModel model;

    @Override
    public void emitQuads(QuadEmitter emitter, BlockRenderView blockView, BlockPos pos, BlockState state, Random random, Predicate<@Nullable Direction> cullTest) {
        if (!state.contains(ChipsBlockHelpers.CHIPS)) {
            return;
        }

        VoxelShape shape = ChipsBlockHelpers.getOutlineShape(state);
        model.emitQuads(emitter, shape);
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
            return new ChipsSpriteInfo(particleSprite, spriteMap);
        });
        this.model = new ChipsModel(spriteSupplier);

//        final SpriteIdentifier spriteIdentifier = new SpriteIdentifier(S.BLOCK_ATLAS_TEXTURE, identifier);
//        this.sprite = spriteGetter.get(spriteIdentifier, () -> "");

        return this;
    }

    @Override
    public Object getEqualityGroup(BlockState state) {
        return null;
    }
}