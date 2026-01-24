package zedzee.github.io.chips.client.mixin.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zedzee.github.io.chips.block.entity.ChipsBlockEntities;
import zedzee.github.io.chips.block.entity.ChipsBlockEntity;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin  {
    @Shadow
    private ClientWorld world;

    @Inject(method = "onBlockEntityUpdate", at = @At("TAIL"))
    public void updateRendering(BlockEntityUpdateS2CPacket packet, CallbackInfo ci) {
        final BlockEntityType<?> type = packet.getBlockEntityType();

        if (type == ChipsBlockEntities.CHIPS_BLOCK_ENTITY) {
            final BlockPos blockPos = packet.getPos();
            BlockState state = world.getBlockState(blockPos);
            world.updateListeners(blockPos, state, state, Block.NOTIFY_ALL_AND_REDRAW);
        }
    }
}
