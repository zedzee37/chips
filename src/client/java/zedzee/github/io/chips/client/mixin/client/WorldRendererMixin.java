package zedzee.github.io.chips.client.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.client.util.BlockBreakStageGetter;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin implements BlockBreakStageGetter {
    @Unique private int stage = 0;

    // this sucks
    @Inject(
            method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/render/block/BlockRenderManager;renderDamage(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;)V"
            )
    )
    public void getProgressStage(RenderTickCounter tickCounter,
                                 boolean renderBlockOutline,
                                 Camera camera,
                                 GameRenderer gameRenderer,
                                 LightmapTextureManager lightmapTextureManager,
                                 Matrix4f matrix4f,
                                 Matrix4f matrix4f2,
                                 CallbackInfo ci,
                                 @Local(ordinal = 0) int o) {
        this.stage = o;
    }

    @Override
    public int getStageToRender() {
        return stage;
    }
}
