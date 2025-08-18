//package zedzee.github.io.chips.client.model;
//
//import com.google.common.base.Suppliers;
//import com.mojang.serialization.MapCodec;
//import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
//import net.minecraft.block.Block;
//import net.minecraft.client.item.ItemModelManager;
//import net.minecraft.client.render.RenderLayers;
//import net.minecraft.client.render.item.ItemRenderState;
//import net.minecraft.client.render.item.model.BasicItemModel;
//import net.minecraft.client.render.item.model.ItemModel;
//import net.minecraft.client.render.model.BakedSimpleModel;
//import net.minecraft.client.render.model.Baker;
//import net.minecraft.client.texture.Sprite;
//import net.minecraft.client.world.ClientWorld;
//import net.minecraft.entity.LivingEntity;
//import net.minecraft.item.BlockItem;
//import net.minecraft.item.ItemDisplayContext;
//import net.minecraft.item.ItemStack;
//import net.minecraft.registry.Registries;
//import net.minecraft.util.Identifier;
//import net.minecraft.util.math.Direction;
//import org.jetbrains.annotations.Nullable;
//import zedzee.github.io.chips.util.ChipsBlockHelpers;
//import zedzee.github.io.chips.util.ChipsItemHelpers;
//
//import java.util.HashMap;
//import java.util.function.Supplier;
//
//// this code is a travesty, beware
//public class ChipsItemModel implements ItemModel.Unbaked, ItemModel {
//    private final ItemModel.Unbaked delegate;
//    private ItemModel originalModel;
//    private ChipsModel model;
//    private Supplier<Baker> bakerSupplier;
//
//    public ChipsItemModel(ItemModel.Unbaked delegate) {
//        this.delegate = delegate;
//    }
//
//    @Override
//    public MapCodec<? extends ItemModel.Unbaked> getCodec() {
//        return delegate.getCodec();
//    }
//
//    @Override
//    public ItemModel bake(ItemModel.BakeContext context) {
//        originalModel = delegate.bake(context);
//        bakerSupplier = Suppliers.memoize(context::blockModelBaker);
//        return this;
//    }
//
//    @Override
//    public void resolve(Resolver resolver) {
//        delegate.resolve(resolver);
//    }
//
//    @Override
//    public void update(ItemRenderState state,
//                       ItemStack stack,
//                       ItemModelManager resolver,
//                       ItemDisplayContext displayContext,
//                       @Nullable ClientWorld world,
//                       @Nullable LivingEntity user,
//                       int seed) {
//        if (
//                !(stack.getItem() instanceof BlockItem blockItem) ||
//                        !shouldRenderChips(stack) ||
//                        !(this.originalModel instanceof BasicItemModel basicItemModel)) {
//            this.originalModel.update(state, stack, resolver, displayContext, world, user, seed);
//            return;
//        }
//
//        if (model == null) {
//            model = createChipModel(blockItem.getBlock());
//        }
//
//        renderModel(state, basicItemModel, stack, displayContext);
//    }
//
//    private boolean shouldRenderChips(ItemStack stack) {
//        int chips = ChipsItemHelpers.getChipsFromStack(stack);
//        return ChipsItemHelpers.stackHasChips(stack) && chips != 255 && chips != 0;
//    }
//
//    private void renderModel(ItemRenderState state,
//                             BasicItemModel basicItemModel,
//                             ItemStack stack,
//                             ItemDisplayContext displayContext) {
//        state.addModelKey(this);
//
//        int chips = ChipsItemHelpers.getChipsFromStack(stack);
//
//        state.addModelKey(chips);
//
//        ItemRenderState.LayerRenderState layer = state.newLayer();
//        QuadEmitter emitter = layer.emitter();
//
//        layer.setRenderLayer(RenderLayers.getItemLayer(stack));
//        basicItemModel.settings.addSettings(layer, displayContext);
//
//        model.emitQuads(emitter, ChipsBlockHelpers.SHAPES[chips]);
//        state.markAnimated();
//    }
//
//    private ChipsModel createChipModel(Block block) {
//        return new ChipsModel(
//                Suppliers.memoize(() -> {
//                            Identifier identifier = Registries.BLOCK.getId(block);
//                            identifier = Identifier.of(identifier.getNamespace(), "block/" + identifier.getPath());
//
//                            BakedSimpleModel model = bakerSupplier.get().getModel(identifier);
//                            Sprite particleSprite = model.getParticleTexture(model.getTextures(), bakerSupplier.get());
//                            HashMap<Direction, Sprite> spriteMap = new HashMap<>();
//                            return new ChipsSpriteInfo(particleSprite, spriteMap);
//                        }
//                ));
//    }
//}