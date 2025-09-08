package zedzee.github.io.chips.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public record ChipsBlockItemComponent(Block block) {
    public static Codec<ChipsBlockItemComponent> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Registries.BLOCK.getCodec().fieldOf("block").forGetter(ChipsBlockItemComponent::block)
            ).apply(builder, ChipsBlockItemComponent::new));
    public static PacketCodec<ByteBuf, Block> BLOCK_PACKET_CODEC = PacketCodec.of(
            ChipsBlockItemComponent::encodeBlock,
            ChipsBlockItemComponent::decodeBlock
    );
    public static PacketCodec<ByteBuf, ChipsBlockItemComponent> PACKET_CODEC = PacketCodec.tuple(
        BLOCK_PACKET_CODEC, ChipsBlockItemComponent::block, ChipsBlockItemComponent::new
    );

    private static void encodeBlock(Block self, ByteBuf buf) {
        Identifier.PACKET_CODEC.encode(buf, Registries.BLOCK.getId(self));
    }

    private static Block decodeBlock(ByteBuf buf) {
        Identifier id = Identifier.PACKET_CODEC.decode(buf);
        return Registries.BLOCK.get(id);
    }

}
