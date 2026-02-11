package zedzee.github.io.chips.render;

import net.minecraft.block.BlockState;
import zedzee.github.io.chips.block.CornerInfo;

import java.util.Set;

public interface RenderData {
    Set<BlockState> getStates();
    CornerInfo getChips(BlockState state);
    boolean shouldUseDefaultUv(BlockState state);
}
    