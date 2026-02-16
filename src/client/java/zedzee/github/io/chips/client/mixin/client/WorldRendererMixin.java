package zedzee.github.io.chips.client.mixin.client;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.BlockBreakingInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.tick.TickManager;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import zedzee.github.io.chips.client.util.BlockBreakStageGetter;

import java.util.Map;
import java.util.SortedSet;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin implements BlockBreakStageGetter {
    @Unique private int stage = 0;

//    // this sucks
//    @Inject(
//            method = "render",
//            at = @At(value = "INVOKE",
//                    target = "Lnet/minecraft/client/render/block/BlockRenderManager;renderDamage(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;)V",
//                    shift = At.Shift.BEFORE
//            ),
//            locals = LocalCapture.CAPTURE_FAILHARD
//    )
//    public void getProgressStage(RenderTickCounter tickCounter,
//                                 boolean renderBlockOutline,
//                                 Camera camera,
//                                 GameRenderer gameRenderer,
//                                 LightmapTextureManager lightmapTextureManager,
//                                 Matrix4f matrix4f,
//                                 Matrix4f matrix4f2,
//                                 CallbackInfo ci,
//                                 TickManager tickManager,
//                                 float f,
//                                 Profiler profiler,
//                                 Vec3d vec3d,
//                                 double d,
//                                 double e,
//                                 double g,
//                                 boolean bl,
//                                 Frustum frustum,
//                                 float h,
//                                 boolean bl2,
//                                 Matrix4fStack matrix4fStack,
//                                 boolean bl3,
//                                 MatrixStack stack,
//                                 VertexConsumerProvider.Immediate immediate,
//                                 Long2ObjectMap.Entry<SortedSet<BlockBreakingInfo>> entry,
//                                 BlockPos pos,
//                                 double l,
//                                 double m,
//                                 double n,
//                                 SortedSet<BlockBreakingInfo> blockBreakingInfos,
//                                 int o) {
//        this.stage = o;
//    }

    @Override
    public int getStageToRender() {
        return stage;
    }
}
