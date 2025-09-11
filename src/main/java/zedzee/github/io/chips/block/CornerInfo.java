package zedzee.github.io.chips.block;

import zedzee.github.io.chips.Chips;

// Standardize passing information about corners.
public record CornerInfo(int index, int shape) {
    public static final CornerInfo EMPTY = new CornerInfo(-1, -1);

    public static CornerInfo fromShape(int shape) {
        int index = Integer.numberOfLeadingZeros(shape);

        if ((1 << index) != shape) {
            Chips.LOGGER.warn("Non corner shape passed.");
        }

        return new CornerInfo(index, shape);
    }

    public static CornerInfo fromIndex(int index) {
        return new CornerInfo(index, 1 << index);
    }

    public boolean exists() {
        return index != -1 && shape != -1;
    }
}
