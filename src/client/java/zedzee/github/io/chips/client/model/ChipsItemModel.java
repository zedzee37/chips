package zedzee.github.io.chips.client.model;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.Block;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.model.BakedSimpleModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.component.ChipsComponents;
import zedzee.github.io.chips.render.RenderData;

import java.util.HashMap;
import java.util.function.Consumer;

// this code is a travesty, beware
public class ChipsItemModel implements ItemModel.Unbaked, ItemModel {
    public static MapCodec<ChipsItemModel> CODEC = MapCodec.unit(new ChipsItemModel());
    private ChipsModel model;

    public ChipsItemModel() {
    }

    @Override
    public MapCodec<? extends ItemModel.Unbaked> getCodec() {
        return CODEC;
    }

    @Override
    public ItemModel bake(ItemModel.BakeContext context) {
        model = new ChipsModel((renderData, tintGetter) -> {
            HashMap<VoxelShape, ChipsSpriteInfo> spriteInfo = new HashMap<>();

            renderData.forEachKey(block -> {
                int chips = renderData.getChips(block);
                VoxelShape shape = ChipsBlock.getShape(chips);

                Identifier identifier = Registries.BLOCK.getId(block);
                identifier = Identifier.of(identifier.getNamespace(), "block/" + identifier.getPath());

                Baker baker = context.blockModelBaker();
                BakedSimpleModel model = baker.getModel(identifier);
                Sprite particleSprite = model.getParticleTexture(model.getTextures(), baker);
                HashMap<Direction, Sprite> spriteMap = new HashMap<>();

                int tint = tintGetter.apply(block);

                spriteInfo.put(shape, new ChipsSpriteInfo(particleSprite, spriteMap, tint));
            });

            return spriteInfo;
        });

        return this;
    }

    @Override
    public void resolve(Resolver resolver) {
    }

    @Override
    public void update(ItemRenderState state,
                       ItemStack stack,
                       ItemModelManager resolver,
                       ItemDisplayContext displayContext,
                       @Nullable ClientWorld world,
                       @Nullable LivingEntity user,
                       int seed) {
        if (!stack.contains(ChipsComponents.BLOCK_COMPONENT_COMPONENT)) {
            return;
        }

        state.addModelKey(this);
        Block block = stack.get(ChipsComponents.BLOCK_COMPONENT_COMPONENT).block();
        state.addModelKey(block);

        ChipsItemRenderData renderData = new ChipsItemRenderData(block);

        ItemRenderState.LayerRenderState layer = state.newLayer();
        QuadEmitter emitter = layer.emitter();

        layer.setRenderLayer(RenderLayers.getItemLayer(stack));

        model.emitQuads(emitter, renderData, blockType -> ColorHelper.getArgb(
                255, 255, 255, 255)
        );
    }

    record ChipsItemRenderData(Block block) implements RenderData {
        @Override
        public int getChips(Block block) {
            return 1;
        }

        @Override
        public void forEachKey(Consumer<Block> consumer) {
            consumer.accept(block);
        }
    }
}