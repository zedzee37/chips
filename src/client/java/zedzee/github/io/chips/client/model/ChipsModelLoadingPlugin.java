package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.util.Identifier;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.ChipsBlocks;

public class ChipsModelLoadingPlugin implements ModelLoadingPlugin {
    public static final Identifier CHIPS_BLOCK_MODEL_ID = Chips.identifier("block/chips");

    @Override
    public void initialize(Context context) {
        context.modifyBlockModelOnLoad().register((original, ctx) ->
                !ctx.state().isOf(ChipsBlocks.CHIPS_BLOCK)
                ? original
                : new ChipsBlockModel()
                );
    }
}
