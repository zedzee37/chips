package zedzee.github.io.chips.client;

import dev.kosmx.playerAnim.api.IPlayable;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.FirstPersonModifier;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.*;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.block.entity.ChipsBlockEntity;
import zedzee.github.io.chips.client.animation.ChipsAnimations;
import zedzee.github.io.chips.client.model.ChipsModelLoadingPlugin;
import zedzee.github.io.chips.networking.ChipsBlockChangePayload;
import zedzee.github.io.chips.networking.ChiselAnimationPayload;

import java.lang.management.MemoryNotificationInfo;

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
            if (animLayer == null || animLayer.isActive()) {
                return;
            }

//            for (String anim : anims.keySet()) {
//                Chips.LOGGER.info(anim);
//            }

            // hardcoded for now no idea what is happenign
            IPlayable playable = PlayerAnimationRegistry.getAnimation(ChipsAnimations.CHIPS_CHISEL_ANIMATION_ID);
//            IPlayable playable = anims.get("chisel");

            if (playable instanceof KeyframeAnimation keyframeAnimation && payload.play()) {
                boolean shouldSwap = shouldSwapHand(context.player());
                if (animLayer.size() == 0 && shouldSwap) {
                    animLayer.addModifier(new MirrorModifier(), 0);
                } else if (animLayer.size() > 0 && !shouldSwap) {
                    animLayer.removeModifier(0);
                }

                KeyframeAnimation.AnimationBuilder builder = keyframeAnimation.mutableCopy();

                animLayer.setAnimation(new KeyframeAnimationPlayer(builder.build()));
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(ChipsBlockChangePayload.ID, (payload, context) -> {
            World world = context.player().getWorld();
            world.setBlockState(payload.pos(), ChipsBlocks.CHIPS_BLOCK.getDefaultState());

            BlockEntity be = world.getBlockEntity(payload.pos());
            if (be instanceof ChipsBlockEntity chipsBlockEntity) {
                chipsBlockEntity.addChips(payload.block(), 255);
            }
        });

//        WorldRenderEvents.LAST.register(context -> {
//            MinecraftClient client = MinecraftClient.getInstance();
//            ClientPlayerEntity player = client.player;
//
//            MatrixStack stack = context.matrixStack();
//            VertexConsumerProvider consumers = context.consumers();
//
//            if (player == null || stack == null || consumers == null) {
//                return;
//            }
//
//            Vec3d direction = Vec3d.fromPolar(player.getPitch(), player.getYaw());
//            Vec3d end = player.getEyePos().add(direction.multiply(player.getBlockInteractionRange()));
//
//            RaycastContext raycastContext = new RaycastContext(
//                    player.getEyePos(),
//                    end,
//                    RaycastContext.ShapeType.COLLIDER,
//                    RaycastContext.FluidHandling.NONE,
//                    player
//            );
//            HitResult hitResult = player.getWorld().raycast(raycastContext);
//
//            if (hitResult == null) {
//                return;
//            }
//
//            stack.push();
//
//            // compensate for camera pos
//            stack.translate(context.camera().getCameraPos().negate());
//
//
//            DebugRenderer.drawBox(
//                    stack,
//                    consumers,
//                    Box.of(hitResult.getPos(), 0.1, 0.1, 0.1),
//                    0.5f,
//                    0.0f,
//                    0.0f,
//                    0.5f
//            );
//
//            stack.pop();
//        });
    }

    private boolean shouldSwapHand(ClientPlayerEntity clientPlayer) {
        return (clientPlayer.getMainArm() == Arm.LEFT && clientPlayer.getActiveHand() == Hand.MAIN_HAND) ||
                (clientPlayer.getMainArm() == Arm.RIGHT && clientPlayer.getActiveHand() == Hand.OFF_HAND);
    }
}
