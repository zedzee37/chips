package zedzee.github.io.chips.client.mixin.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zedzee.github.io.chips.block.ChipsBlock;

import java.util.Set;

@Mixin(ClientPlayerInteractionManager.class)
public class BreakSoundMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "updateBlockBreakingProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/SoundManager;play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;"))
    public void playCorrectSound(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        assert client.world != null;

        Set<BlockSoundGroup> soundGroups = ChipsBlock.getSoundGroups(client.world, pos);

        soundGroups.forEach(soundGroup -> {
            client
                    .getSoundManager()
                    .play(

                            new PositionedSoundInstance(
                                    soundGroup.getHitSound(),
                                    SoundCategory.BLOCKS,
                                    (soundGroup.getVolume() + 1.0F) / 8.0F,
                                    soundGroup.getPitch() * 0.5F,
                                    SoundInstance.createRandom(),
                                    pos
                            )
                    );
        });
    }
}
