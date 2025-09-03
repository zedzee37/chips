package zedzee.github.io.chips.client.mixin.client;

import com.mojang.authlib.GameProfile;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zedzee.github.io.chips.animation.ChipsAnimatedPlayer;

@Mixin(AbstractClientPlayerEntity.class)
public class AnimatedClientPlayerMixin implements ChipsAnimatedPlayer {
    @Unique private final ModifierLayer<IAnimation> animationLayer = new ModifierLayer<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initAnimationLayer(ClientWorld world, GameProfile profile, CallbackInfo ci) {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        PlayerAnimationAccess.getPlayerAnimLayer(player).addAnimLayer(1, animationLayer);
    }

    @Override
    public ModifierLayer<IAnimation> chips_getModAnimation() {
        return animationLayer;
    }
}
