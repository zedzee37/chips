package zedzee.github.io.chips.util;

import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.block.CornerInfo;

public interface ChipsBlockBreakingProgress {
    @Nullable
    CornerInfo chips$getCorner();
    void chips$setCorner(CornerInfo cornerInfo);
}
