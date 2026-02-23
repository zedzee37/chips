package zedzee.github.io.chips.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import zedzee.github.io.chips.Chips;

public record BlockSplitPayload(BlockPos pos) implements CustomPayload {
    public static final Identifier IDENTIFIER = Chips.identifier("block_split");
    public static final Id<BlockSplitPayload> ID = new Id<>(IDENTIFIER);
    public static final PacketCodec<ByteBuf, BlockSplitPayload> PACKET_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, BlockSplitPayload::pos,
            BlockSplitPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
