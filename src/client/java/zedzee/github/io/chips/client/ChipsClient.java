package zedzee.github.io.chips.client;

import dev.kosmx.playerAnim.api.IPlayable;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.block.CornerInfo;
import zedzee.github.io.chips.block.entity.ChipsBlockEntity;
import zedzee.github.io.chips.client.animation.ChipsAnimations;
import zedzee.github.io.chips.client.model.ChipsModelLoadingPlugin;
import zedzee.github.io.chips.networking.ChipsBlockChangePayload;
import zedzee.github.io.chips.networking.ChiselAnimationPayload;

public class ChipsClient implements ClientModInitializer {
    private final float ANIMATION_SPEED = 0.2f;
    private float progress = 0.0f;

    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(new ChipsModelLoadingPlugin());
//        ChipsAnimations.init();
////        ChiselingStationScreen.register();
////        TestExtraModel.register();
//

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

        WorldRenderEvents.BLOCK_OUTLINE.register(
                ((worldRenderContext, blockOutlineContext) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player == null) {
                        return true;
                    }

                    boolean hasMace =
                            client.player.getOffHandStack().isOf(Items.MACE) ||
                                    client.player.getMainHandStack().isOf(Items.MACE);

                    if (blockOutlineContext.blockState().isOf(ChipsBlocks.CHIPS_BLOCK) || !hasMace) {
                        return true;
                    }

                    BlockPos pos = blockOutlineContext.blockPos();
                    VoxelShape outlineShape = blockOutlineContext.blockState().getOutlineShape(
                            worldRenderContext.world(),
                            pos,
                            ShapeContext.of(blockOutlineContext.entity())
                    );

                    if (!outlineShape.equals(VoxelShapes.fullCube())) {
                        return true;
                    }

                    float tickDelta = worldRenderContext.tickCounter().getTickDelta(false);
                    progress += (tickDelta / 20.0f) * ANIMATION_SPEED;
                    float anim = MathHelper.sin(progress);

                    for (int i = 0; i < ChipsBlock.CORNER_SHAPES.length; i++) {
                        VoxelShape cornerShape = ChipsBlock.CORNER_SHAPES[i];
                        WorldRenderer.drawCuboidShapeOutline(
                                worldRenderContext.matrixStack(),
                                blockOutlineContext.vertexConsumer(),
                                cornerShape,
                                pos.getX() - blockOutlineContext.cameraX(),
                                pos.getY() - blockOutlineContext.cameraY(),
                                pos.getZ() - blockOutlineContext.cameraZ(),
                                1.0f,
                                1.0f,
                                1.0f,
                                Math.clamp(anim, 0.3f, 0.6f)
                        );
                    }

                    if (progress >= MathHelper.PI) {
                        progress = 0.0f;
                    }

                    return false;
                })
        );

        ClientPlayNetworking.registerGlobalReceiver(ChipsBlockChangePayload.ID, (payload, context) -> {
            World world = context.player().getWorld();
            world.setBlockState(payload.pos(), ChipsBlocks.CHIPS_BLOCK.getDefaultState());

            BlockEntity be = world.getBlockEntity(payload.pos());
            if (be instanceof ChipsBlockEntity chipsBlockEntity) {
                chipsBlockEntity.addChips(payload.state(), CornerInfo.fromShape(255));
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
