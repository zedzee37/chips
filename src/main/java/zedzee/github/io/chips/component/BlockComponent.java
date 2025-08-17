package zedzee.github.io.chips.component;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;

public record BlockComponent(Block block) {
    public static Codec<BlockComponent> CODEC = Codec.of(Block.CODEC);
}
