package zedzee.github.io.chips.block;

// Standardize passing information about corners.
public record CornerInfo(int index, int corner) {
    public static CornerInfo fromCorner(int corner) {
        return new CornerInfo(Integer.numberOfLeadingZeros(corner) + 1, corner);
    }

    public static CornerInfo fromIndex(int index) {
        return new CornerInfo(index, 1 << index);
    }
}
