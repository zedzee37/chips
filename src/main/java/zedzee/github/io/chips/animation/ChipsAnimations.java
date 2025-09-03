package zedzee.github.io.chips.animation;

import dev.kosmx.playerAnim.api.IPlayable;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.util.Identifier;
import zedzee.github.io.chips.Chips;

public class ChipsAnimations {
    public static final Identifier CHIPS_CHISEL_ANIMATION_ID = Chips.identifier("chisel");
    public static final IPlayable CHIPS_CHISEL_ANIMATION = PlayerAnimationRegistry.getAnimation(CHIPS_CHISEL_ANIMATION_ID);

    public static void init() {}
}
