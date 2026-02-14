package zedzee.github.io.chips.client.mixin.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.ChipsBlocks;

import java.util.List;

@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {
    @Unique
    private static final List<Identifier> CHIPS_DESTRUCTION_STAGE_TEXTURES = List.of(
            Chips.identifier("textures/block/chiseling_station.png")
    );
    @Unique
    private static final List<RenderLayer> CHIPS_DESTRUCTION_LAYERS = CHIPS_DESTRUCTION_STAGE_TEXTURES.stream()
            .map(RenderLayer::getBlockBreaking)
            .toList();

    @Shadow
    @Final
    private BlockModelRenderer blockModelRenderer;

    @Shadow
    @Final
    private BlockModels models;

    @Shadow
    @Final
    private Random random;

    @Inject(method = "renderDamage",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/render/block/BlockModelRenderer;render(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLnet/minecraft/util/math/random/Random;JI)V",
                    shift = At.Shift.BEFORE),
            cancellable = true
    )
    public void renderChipDamage(BlockState state,
                                 BlockPos pos,
                                 BlockRenderView world,
                                 MatrixStack matrices,
                                 VertexConsumer vertexConsumer,
                                 CallbackInfo ci) {
        if (!state.isOf(ChipsBlocks.CHIPS_BLOCK)) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        assert client != null;

        WorldRenderer worldRenderer = client.worldRenderer;

        MatrixStack.Entry entry = matrices.peek();
        VertexConsumer consumer = new OverlayVertexConsumer(
                worldRenderer.bufferBuilders
                        .getEffectVertexConsumers()
                        .getBuffer(CHIPS_DESTRUCTION_LAYERS.get(0)),
                entry,
                1.0F
        );

        BakedModel bakedModel = this.models.getModel(state);
        long l = state.getRenderingSeed(pos);
        blockModelRenderer.render(world,
                bakedModel,
                state,
                pos,
                matrices,
                consumer,
                true,
                this.random,
                l,
                OverlayTexture.DEFAULT_UV);
        ci.cancel();
    }
}
