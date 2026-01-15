package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.item.ChipsItems;

public class ChipsModelLoadingPlugin implements ModelLoadingPlugin {

    private boolean itemIdIsItem(Identifier itemId, Item item) {
        return itemId.equals(Registries.ITEM.getId(item));
    }

    @Override
    public void onInitializeModelLoader(Context context) {
        context.modifyModelAfterBake().register((original, ctx) ->
                ctx.resourceId().equals()
                        ? new ChipsBlockModel()
                        : original
        );
//
        context.modifyItemModelBeforeBake().register((original, ctx) ->
                itemIdIsItem(ctx.itemId(), ChipsItems.CHIPS_BLOCK_ITEM) || itemIdIsItem(ctx.itemId(), ChipsItems.TEST_BLOCK_ITEM) ?
                        new ChipsItemModel() : original
        );
    }
//    private boolean shouldRenderChipsModel(BlockState state) {
//        return state.contains(ChipsBlockHelpers.CHIPS) && state.get(ChipsBlockHelpers.CHIPS) != 255 && state.get(ChipsBlockHelpers.CHIPS) != 0;
//    }
}
