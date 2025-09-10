package zedzee.github.io.chips.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zedzee.github.io.chips.block.ChipsBlock;

import java.util.Set;


@Mixin(Entity.class)
public class EntityStepSoundMixin {
    @Shadow private World world;

    @Inject(method = "playStepSound", at = @At("HEAD"))
   public void playCorrectStepSound(BlockPos pos, BlockState state, CallbackInfo ci) {
        Set<BlockSoundGroup> soundGroups = ChipsBlock.getSoundGroups(world, pos);

        Entity entity = (Entity)(Object)this;

        soundGroups.forEach(soundGroup ->
                entity.playSound(soundGroup.getStepSound(),
                        soundGroup.getVolume() * 0.15F,
                        soundGroup.getPitch())
        );
   }
}
