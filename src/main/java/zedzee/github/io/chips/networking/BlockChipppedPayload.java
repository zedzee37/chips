package zedzee.github.io.chips.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.CornerInfo;

public record BlockChipppedPayload(BlockPos blockPos, CornerInfo cornerInfo) implements CustomPayload {
    public static final Identifier IDENTIFIER = Chips.identifier("chips_block_chipped");
    public static final CustomPayload.Id<BlockChipppedPayload> ID = new CustomPayload.Id<>(IDENTIFIER);
    public static final PacketCodec<ByteBuf, BlockChipppedPayload> PACKET_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, BlockChipppedPayload::blockPos,
            CornerInfo.PACKET_CODEC, BlockChipppedPayload::cornerInfo,
            BlockChipppedPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
