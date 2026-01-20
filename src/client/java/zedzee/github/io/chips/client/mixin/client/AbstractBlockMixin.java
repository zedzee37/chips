package zedzee.github.io.chips.client.mixin.client;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.block.CornerInfo;
import zedzee.github.io.chips.client.util.ChipsBlockBreakingProgress;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    @Inject(method = "onBlockBreakStart", at = @At("HEAD"))
    public void setChipsCorner(BlockState state, World world, BlockPos pos, PlayerEntity player, CallbackInfo ci) {
        if (!state.isOf(ChipsBlocks.CHIPS_BLOCK) || !world.isClient) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ChipsBlockBreakingProgress blockBreakingProgress = (ChipsBlockBreakingProgress)client.interactionManager;
        CornerInfo hoveredCorner = ChipsBlock.getHoveredCorner(world, player);
        blockBreakingProgress.chips$setCorner(hoveredCorner);
    }
}
