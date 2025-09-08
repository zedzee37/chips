package zedzee.github.io.chips.networking;

import net.minecraft.block.Block;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.component.ChipsBlockItemComponent;

public record ChipsBlockChangePayload(BlockPos pos, Block block) implements CustomPayload {
    public static final Identifier IDENTIFIER = Chips.identifier("chips_block_change");
    public static final CustomPayload.Id<ChipsBlockChangePayload> ID = new CustomPayload.Id<>(IDENTIFIER);
    public static final PacketCodec<RegistryByteBuf, ChipsBlockChangePayload> CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC, ChipsBlockChangePayload::pos,
                    ChipsBlockItemComponent.BLOCK_PACKET_CODEC, ChipsBlockChangePayload::block,
                    ChipsBlockChangePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
