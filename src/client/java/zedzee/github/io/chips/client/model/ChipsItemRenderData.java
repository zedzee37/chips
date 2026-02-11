package zedzee.github.io.chips.client.model;


import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import zedzee.github.io.chips.block.CornerInfo;
import zedzee.github.io.chips.render.RenderData;

import java.util.Set;

record ChipsItemRenderData(Block block) implements RenderData {
    private final static CornerInfo DEFAULT_ITEM_CHIPS = CornerInfo.fromShape(64);

    @Override
    public CornerInfo getChips(BlockState state) {
        return DEFAULT_ITEM_CHIPS;
    }

    @Override
    public boolean shouldUseDefaultUv(BlockState state) {
        return false;
    }

    public Set<BlockState> getStates() {
        return Set.of(block.getDefaultState());
    }
}
