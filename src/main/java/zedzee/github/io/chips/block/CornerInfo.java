package zedzee.github.io.chips.block;

import zedzee.github.io.chips.Chips;

// Standardize passing information about corners.
public record CornerInfo(int index, int corner) {

    // TODO: test if this needs index + 1
    public static CornerInfo fromCorner(int corner) {
        int index = Integer.numberOfLeadingZeros(corner);

        if ((1 << index) != corner) {
            Chips.LOGGER.warn("Non corner shape passed.");
        }

        return new CornerInfo(index, corner);
    }

    public static CornerInfo fromIndex(int index) {
        return new CornerInfo(index, 1 << index);
    }
}
