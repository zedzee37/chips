package zedzee.github.io.chips.client;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import zedzee.github.io.chips.client.animation.ChipsAnimations;
import zedzee.github.io.chips.client.model.ChipsModelLoadingPlugin;
import zedzee.github.io.chips.networking.ChiselAnimationPayload;

public class ChipsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(new ChipsModelLoadingPlugin());
        ChipsAnimations.init();
//        ChiselingStationScreen.register();
//        TestExtraModel.register();

//        WorldRenderEvents.BLOCK_OUTLINE.register(
//                (worldRenderContext, blockOutlineContext) -> {
//        });

        ClientPlayNetworking.registerGlobalReceiver(ChiselAnimationPayload.ID, (payload, context) -> {
            ModifierLayer<IAnimation> animLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                    .getPlayerAssociatedData(context.player())
                    .get(ChipsAnimations.CHIPS_ANIMATOR_ID);
            animLayer.setAnimation(new KeyframeAnimationPlayer((KeyframeAnimation) PlayerAnimationRegistry.getAnimation(ChipsAnimations.CHIPS_CHISEL_ANIMATION_ID)));
        });
    }
}
