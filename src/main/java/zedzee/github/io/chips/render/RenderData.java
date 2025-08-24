package zedzee.github.io.chips.render;

import net.minecraft.block.Block;

import java.util.function.Consumer;

public interface RenderData {
    int getChips(Block block);
    void forEachBlock(Consumer<Block> consumer);
    boolean shouldUseDefaultUv(Block block);
}
