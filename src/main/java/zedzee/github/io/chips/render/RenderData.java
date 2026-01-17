package zedzee.github.io.chips.render;

import net.minecraft.block.Block;

import java.util.Set;
import java.util.function.Consumer;

public interface RenderData {
    int getChips(Block block);
    Set<Block> getBlocks();
    boolean shouldUseDefaultUv(Block block);
}
    