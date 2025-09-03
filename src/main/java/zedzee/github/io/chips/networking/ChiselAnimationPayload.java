package zedzee.github.io.chips.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import zedzee.github.io.chips.Chips;

public record ChiselAnimationPayload(boolean play) implements CustomPayload {
    public static final Identifier IDENTIFIER = Chips.identifier("chisel");
    public static final CustomPayload.Id<ChiselAnimationPayload> ID = new CustomPayload.Id<>(IDENTIFIER);
    public static final PacketCodec<RegistryByteBuf, ChiselAnimationPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.BOOLEAN, ChiselAnimationPayload::play, ChiselAnimationPayload::new);
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
