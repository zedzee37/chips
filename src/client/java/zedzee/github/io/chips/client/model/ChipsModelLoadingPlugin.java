package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.ChipsBlocks;


public class ChipsModelLoadingPlugin implements ModelLoadingPlugin {
    public static final ModelIdentifier CHIPS_BLOCK_MODEL_INVENTORY_ID = new ModelIdentifier(
            Chips.identifier("chips_block"), "inventory"
    );


//    private boolean itemIdIsItem(Identifier itemId, Item item) {
//        return itemId.equals(Registries.ITEM.getId(item));
//    }

    @Override
    public void onInitializeModelLoader(Context context) {
        context.modifyModelOnLoad().register((original, ctx) -> {
            final ModelIdentifier modelId = ctx.topLevelId();

            if (modelId != null) {
                Identifier id = modelId.id();

                if (
                        id.equals(Registries.BLOCK.getId(ChipsBlocks.CHIPS_BLOCK)) ||
                        modelId.equals(CHIPS_BLOCK_MODEL_INVENTORY_ID)
                ) {
                    return new ChipsBlockModel();
                }
            }
            return original;
        });
//
//        context.modifyItemModelBeforeBake().register((original, ctx) ->
//                itemIdIsItem(ctx.itemId(), ChipsItems.CHIPS_BLOCK_ITEM) || itemIdIsItem(ctx.itemId(), ChipsItems.TEST_BLOCK_ITEM) ?
//                        new ChipsItemModel() : original
//        );
    }
//    private boolean shouldRenderChipsModel(BlockState state) {
//        return state.contains(ChipsBlockHelpers.CHIPS) && state.get(ChipsBlockHelpers.CHIPS) != 255 && state.get(ChipsBlockHelpers.CHIPS) != 0;
//    }
}
