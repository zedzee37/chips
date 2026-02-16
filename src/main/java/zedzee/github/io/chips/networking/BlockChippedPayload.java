package zedzee.github.io.chips.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.CornerInfo;

public record BlockChippedPayload(BlockPos blockPos, CornerInfo cornerInfo, boolean shouldDrop) implements CustomPayload {
    public static final Identifier IDENTIFIER = Chips.identifier("chips_block_chipped");
    public static final CustomPayload.Id<BlockChippedPayload> ID = new CustomPayload.Id<>(IDENTIFIER);
    public static final PacketCodec<ByteBuf, BlockChippedPayload> PACKET_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, BlockChippedPayload::blockPos,
            CornerInfo.PACKET_CODEC, BlockChippedPayload::cornerInfo,
            PacketCodecs.BOOL, BlockChippedPayload::shouldDrop,
            BlockChippedPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
