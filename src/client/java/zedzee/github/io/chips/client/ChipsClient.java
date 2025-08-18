package zedzee.github.io.chips.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import zedzee.github.io.chips.client.model.ChipsModelLoadingPlugin;
import zedzee.github.io.chips.client.model.TestExtraModel;
import zedzee.github.io.chips.client.screen.ChiselingStationScreen;

public class ChipsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(new ChipsModelLoadingPlugin());
        ChiselingStationScreen.register();
//        TestExtraModel.register();

//        WorldRenderEvents.BLOCK_OUTLINE.register(
//                (worldRenderContext, blockOutlineContext) -> {
//        });
    }
}
