package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import zedzee.github.io.chips.Chips;

public class ChipsModelLoadingPlugin implements ModelLoadingPlugin {
    public static final ModelIdentifier CHIPS_BLOCK_MODEL_ID = new ModelIdentifier(Chips.identifier("block/chips"), "");

    @Override
    public void initialize(Context context) {
        context.modifyModelOnLoad().register((original, ctx) -> {
            final Identifier id = ctx.id();
            if (id != null && id.equals(CHIPS_BLOCK_MODEL_ID)) {
                return new ChipsBlockModel();
            } else {
                return original;
            }
        });
    }
}
