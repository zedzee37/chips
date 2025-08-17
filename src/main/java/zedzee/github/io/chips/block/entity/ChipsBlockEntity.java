package zedzee.github.io.chips.block.entity;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import zedzee.github.io.chips.block.ChipsBlock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChipsBlockEntity extends BlockEntity implements RenderDataBlockEntity {
    private int chips = ChipsBlock.DEFAULT_CHIPS_VALUE;
    private Map<Block, Integer> blockMap = Map.of(Blocks.DIAMOND_BLOCK, ChipsBlock.DEFAULT_CHIPS_VALUE);

    public ChipsBlockEntity(BlockPos pos, BlockState state) {
        super(ChipsBlockEntities.CHIPS_BLOCK_ENTITY, pos, state);
    }

    public int getChips() {
        return chips;
    }

    public void setChips(int chips) {
        this.chips = chips;
        markDirty();
        sync();
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        sync();
        return createNbt(registryLookup);
    }

//    @Override
//    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
//        return BlockEntityUpdateS2CPacket.create(this);
//    }

    public void sync() {
        if (world == null || world.isClient) {
            return;
        }

        world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);

        markDirty();
        ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
    }

    @Override
    protected void writeData(WriteView view) {
        view.put("chips", Codec.INT, chips);

        WriteView.ListAppender<Block> blockListAppender = view.getListAppender("blocks", Registries.BLOCK.getCodec());
        WriteView.ListAppender<Integer> mappedChipsListAppender = view.getListAppender(
                "mappedChips", Codec.INT
        );

        blockMap.forEach((block, chipMap) -> {
            blockListAppender.add(block);
            mappedChipsListAppender.add(chipMap);
        });

        super.writeData(view);
    }

    @Override
    protected void readData(ReadView view) {
        this.chips = view.read("chips", Codec.INT).orElse(ChipsBlock.DEFAULT_CHIPS_VALUE);
        this.blockMap = new HashMap<>();

        ReadView.TypedListReadView<Block> blockList = view.getTypedListView("blocks", Registries.BLOCK.getCodec());
        ReadView.TypedListReadView<Integer> mappedChipsList = view.getTypedListView(
                "mappedChips", Codec.INT
        );

        this.blockMap = zipBlockMap(blockList, mappedChipsList).orElse(Map.of());
        sync();
        super.readData(view);
    }

    private Optional<Map<Block, Integer>> zipBlockMap(
            ReadView.TypedListReadView<Block> blockList,
            ReadView.TypedListReadView<Integer> mappedChipsList) {
        List<Block> blocks = blockList.stream().toList();
        List<Integer> mappedChips = mappedChipsList.stream().toList();

        if (blocks.size() != mappedChips.size()) {
            return Optional.empty();
        }
        Map<Block, Integer> blockIntegerMap = IntStream.range(0, blocks.size())
                .boxed()
                .collect(Collectors.toMap(
                        blocks::get,
                        mappedChips::get
                ));

        return Optional.of(blockIntegerMap);
    }
}
