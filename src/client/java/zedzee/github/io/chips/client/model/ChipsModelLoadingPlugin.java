package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.ChipsBlockHelpers;

public class ChipsModelLoadingPlugin implements ModelLoadingPlugin {
    public static final Identifier CHIPS_BLOCK_MODEL_ID = Chips.identifier("block/chips");

    @Override
    public void initialize(Context context) {
        context.modifyBlockModelOnLoad().register((original, ctx) ->
                !ctx.state().contains(ChipsBlockHelpers.CHIPS)
                ? original
                : new ChipsBlockModel()
                );
    }

    private boolean shouldRenderChipsModel(BlockState state) {
//        return state.contains(ChipsBlockHelpers.CHIPS) && state.get(ChipsBlockHelpers.CHIPS) != 255;
// for testing purposes
        return state.contains(ChipsBlockHelpers.CHIPS) && state.get(ChipsBlockHelpers.CHIPS) != 255;
    }
}
