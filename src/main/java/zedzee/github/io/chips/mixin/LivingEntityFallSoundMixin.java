package zedzee.github.io.chips.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zedzee.github.io.chips.block.ChipsBlock;

import java.util.Set;

@Mixin(LivingEntity.class)
public class LivingEntityFallSoundMixin {
    @Inject(method = "playBlockFallSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"))
    public void playChipsFallSound(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;

        int i = MathHelper.floor(entity.getX());
        int j = MathHelper.floor(entity.getY() - 0.2F);
        int k = MathHelper.floor(entity.getZ());

        BlockPos pos = new BlockPos(i, j, k);
        Set<BlockSoundGroup> soundGroups = ChipsBlock.getSoundGroups(entity.getWorld(), pos);

        soundGroups.forEach(soundGroup ->
                entity.playSound(soundGroup.getFallSound(),
                        soundGroup.getVolume() * 0.5F,
                        soundGroup.getPitch() * 0.75F));
    }
}
