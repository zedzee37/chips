package zedzee.github.io.chips.client.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public abstract class DebugRayMarchRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    public void renderRayMarch(MatrixStack matrices,
                               Frustum frustum,
                               VertexConsumerProvider.Immediate vertexConsumers,
                               double cameraX,
                               double cameraY,
                               double cameraZ,
                               CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player != null) {
            DebugRenderer.drawBox(matrices, vertexConsumers, new Box(
                    player.getPos(),
                    player.getPos().add(10, 10, 10)
            ), 255, 255, 255, 255);
        }
    }
}
