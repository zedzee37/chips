package zedzee.github.io.chips.client.model;

import net.minecraft.block.BlockState;

@FunctionalInterface
public interface BlockColorProvider {
    int getColor(BlockState state, int tintIndex);
}
