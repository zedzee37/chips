package zedzee.github.io.chips.client.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.block.CornerInfo;
import zedzee.github.io.chips.block.entity.ChipsBlockEntity;
import zedzee.github.io.chips.item.ChipsBlockItem;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    @Nullable
    public HitResult crosshairTarget;

    @Shadow
    @Nullable
    public ClientWorld world;

    @ModifyVariable(
            method = "doItemPick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getInventory()Lnet/minecraft/entity/player/PlayerInventory;"),
            name = "itemStack"
    )
    public ItemStack modifyPickedStack(ItemStack itemStack) {
        assert crosshairTarget != null;
        if (crosshairTarget.getType() != HitResult.Type.BLOCK) return itemStack;

        final BlockHitResult blockHitResult = (BlockHitResult) crosshairTarget;
        final BlockPos pos = blockHitResult.getBlockPos();

        if (world == null) return itemStack;
        final BlockState state = world.getBlockState(pos);
        if (!state.isOf(ChipsBlocks.CHIPS_BLOCK)) return itemStack;

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof ChipsBlockEntity chipsBlockEntity)) return itemStack;

        CornerInfo closestCorner = ChipsBlock.getClosestSlice(world, pos, blockHitResult.getPos());
        if (closestCorner == null || !closestCorner.exists()) return itemStack;

        BlockState cornerState = chipsBlockEntity.getStateAtCorner(closestCorner);

        if (cornerState == null) return itemStack;

        return ChipsBlockItem.getStack(cornerState.getBlock());
    }
}
