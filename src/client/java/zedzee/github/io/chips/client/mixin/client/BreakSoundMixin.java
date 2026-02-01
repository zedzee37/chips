package zedzee.github.io.chips.client.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientPlayerInteractionManager.class)
public class BreakSoundMixin {
    @Shadow @Final private MinecraftClient client;
// todo: fix this
//    @Inject(method = "updateBlockBreakingProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/SoundManager;play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;"))
//    public void playCorrectSound(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
//        assert client.world != null;
//
//        Set<BlockSoundGroup> soundGroups = ChipsBlock.getSoundGroups(client.world, pos);
//
//        soundGroups.forEach(soundGroup -> {
//            client
//                    .getSoundManager()
//                    .play(
//
//                            new PositionedSoundInstance(
//                                    soundGroup.getHitSound(),
//                                    SoundCategory.BLOCKS,
//                                    (soundGroup.getVolume() + 1.0F) / 8.0F,
//                                    soundGroup.getPitch() * 0.5F,
//                                    SoundInstance.createRandom(),
//                                    pos
//                            )
//                    );
//        });
//    }
}
