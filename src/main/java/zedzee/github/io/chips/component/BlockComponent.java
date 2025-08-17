package zedzee.github.io.chips.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.Optional;

public record BlockComponent(Block block) {
    public static Codec<BlockComponent> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Registries.BLOCK.getCodec().fieldOf("block").forGetter(BlockComponent::block)
            ).apply(builder, BlockComponent::new));
    public static PacketCodec<ByteBuf, BlockComponent> PACKET_CODEC = PacketCodec.of(
            BlockComponent::encode,
            BlockComponent::decode
    );


    private static void encode(BlockComponent self, ByteBuf buf) {
        Identifier.PACKET_CODEC.encode(buf, Registries.BLOCK.getId(self.block()));
    }

    private static BlockComponent decode(ByteBuf buf) {
        Identifier id = Identifier.PACKET_CODEC.decode(buf);
        return new BlockComponent(Registries.BLOCK.get(id));
    }

}
