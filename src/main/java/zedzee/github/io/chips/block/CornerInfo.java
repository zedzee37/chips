package zedzee.github.io.chips.block;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import zedzee.github.io.chips.Chips;

// Standardize passing information about corners.
public record CornerInfo(int index, int shape) {
    public static final PacketCodec<ByteBuf, CornerInfo> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, CornerInfo::index,
            PacketCodecs.INTEGER, CornerInfo::shape,
            CornerInfo::new
    );

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

    public boolean isEmpty() {
        return index == 0 || shape == 0;
    }

    public CornerInfo union(CornerInfo other) {
        return CornerInfo.fromShape(this.shape() | other.shape());
    }

    public CornerInfo removeShape(CornerInfo shape) {
        return CornerInfo.fromShape(this.shape & ~shape.shape());
    }

    public boolean hasShape(CornerInfo corner) {
        return (shape() | corner.shape()) == shape();
    }

    public boolean isFull() {
        return this.shape() == 255;
    }
}
