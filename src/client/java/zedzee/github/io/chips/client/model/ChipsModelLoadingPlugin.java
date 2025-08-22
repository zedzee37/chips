package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.registry.Registries;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.item.ChipsItems;

public class ChipsModelLoadingPlugin implements ModelLoadingPlugin {
    @Override
    public void initialize(Context context) {

        context.modifyBlockModelOnLoad().register((original, ctx) ->
                ctx.state().isOf(ChipsBlocks.CHIPS_BLOCK)
                        ? new TestBlockModel()
                        : original
        );

        context.modifyItemModelBeforeBake().register((original, ctx) ->
                ctx.itemId().equals(Registries.ITEM.getId(ChipsItems.CHIPS_BLOCK_ITEM)) ?
                        new ChipsItemModel() : original
        );
    }
//    private boolean shouldRenderChipsModel(BlockState state) {
//        return state.contains(ChipsBlockHelpers.CHIPS) && state.get(ChipsBlockHelpers.CHIPS) != 255 && state.get(ChipsBlockHelpers.CHIPS) != 0;
//    }
}
