package zedzee.github.io.chips.client.animation;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import zedzee.github.io.chips.Chips;

public class ChipsAnimations {
    public static final Identifier CHIPS_ANIMATOR_ID = Chips.identifier("animator");
    public static final Identifier CHIPS_CHISEL_ANIMATION_ID = Chips.identifier("chisel");
    public static final int PRIORITY = 42;

    public static void init() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(CHIPS_ANIMATOR_ID, PRIORITY, player -> {
            if (player instanceof ClientPlayerEntity) {
                ModifierLayer<IAnimation> animLayer = new ModifierLayer<>();
                return animLayer;
            }
            return null;
        });

        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player, animationStack) -> {
            ModifierLayer<IAnimation> layer = new ModifierLayer<>();
            animationStack.addAnimLayer(PRIORITY, layer);
            PlayerAnimationAccess.getPlayerAssociatedData(player).set(CHIPS_CHISEL_ANIMATION_ID, layer);
        });
    }
}
