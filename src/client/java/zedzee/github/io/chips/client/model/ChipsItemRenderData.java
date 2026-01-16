package zedzee.github.io.chips.client.model;


import net.minecraft.block.Block;
import zedzee.github.io.chips.render.RenderData;

import java.util.function.Consumer;

record ChipsItemRenderData(Block block) implements RenderData {
    private final static int DEFAULT_ITEM_CHIPS = 64;

    @Override
    public int getChips(Block block) {
        return DEFAULT_ITEM_CHIPS;
    }

    @Override
    public void forEachBlock(Consumer<Block> consumer) {
        consumer.accept(block);
    }

    @Override
    public boolean shouldUseDefaultUv(Block block) {
        return false;
    }
}
