package zedzee.github.io.chips.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

// Standardize passing information about corners.
public record CornerInfo(int index, int shape) {
    public static final PacketCodec<ByteBuf, CornerInfo> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, CornerInfo::index,
            PacketCodecs.INTEGER, CornerInfo::shape,
            CornerInfo::new
    );
    public static final Codec<CornerInfo> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(
                    Codec.INT.fieldOf("shape").forGetter(CornerInfo::shape)
            ).apply(builder, CornerInfo::fromShape)
    );

    public static final CornerInfo EMPTY = new CornerInfo(-1, -1);

    public static CornerInfo fromShape(int shape) {
        int index = Integer.numberOfTrailingZeros(shape);
        return new CornerInfo(index, shape);
    }

    public static CornerInfo fromIndex(int index) {
        return new CornerInfo(index, 1 << index);
    }

    public boolean exists() {
        return index != -1 && shape != -1;
    }

    public boolean isEmpty() {
        return shape == 0;
    }

    public CornerInfo union(CornerInfo other) {
        return CornerInfo.fromShape(this.shape() | other.shape());
    }

    public CornerInfo removeShape(CornerInfo shape) {
        return CornerInfo.fromShape(this.shape() & ~shape.shape());
    }

    public boolean hasShape(CornerInfo corner) {
        return (shape() | corner.shape()) == shape();
    }

    public boolean isFull() {
        return this.shape() == 255;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CornerInfo otherCorner)) {
            return false;
        }

        return this.shape() == otherCorner.shape();
    }
}
