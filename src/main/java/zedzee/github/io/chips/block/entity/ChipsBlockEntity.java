package zedzee.github.io.chips.block.entity;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.render.RenderData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChipsBlockEntity extends BlockEntity implements RenderDataBlockEntity {
    private Map<Block, Integer> blockMap = new HashMap<>();

    public ChipsBlockEntity(BlockPos pos, BlockState state) {
        super(ChipsBlockEntities.CHIPS_BLOCK_ENTITY, pos, state);
    }

    public int getTotalChips() {
        int total = 0;
        for (int value : blockMap.values()) {
            total |= value;
        }
        return total;
    }

    public int getChips(Block block) {
        return this.blockMap.get(block);
    }

    public void forEachKey(Consumer<Block> blockConsumer) {
        blockMap.keySet().forEach(blockConsumer);
    }

    public void setChips(Block block, int chips) {
        this.blockMap.put(block, chips);
        markDirty();
        sync();
    }

    public void clear() {
        this.blockMap.clear();
        markDirty();
        sync();
    }

    @Override
    public @Nullable Object getRenderData() {
        return new ChipsRenderData(blockMap);
    }

    public boolean hasCorner(int corner) {
        return (getTotalChips() & corner) != 0;
    }

    public void addChips(Block block, int chips) {
        int currentChips = 0;
        if (blockMap.containsKey(block)) {
            currentChips = blockMap.get(block);
        }

        blockMap.put(block, currentChips | chips);
        setChips(block, currentChips | chips);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        sync();
        return createNbt(registryLookup);
    }

//    @Override
//    public @Nullable Object getRenderData() {
//        return blockMap;
//    }

    //    @Override
//    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
//        return BlockEntityUpdateS2CPacket.create(this);
//    }

    public void sync() {
        if (world == null) {
            return;
        }

        world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);

        if (!world.isClient) {
            markDirty();
            ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
        }
    }

    @Override
    protected void writeData(WriteView view) {
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
        this.blockMap = new HashMap<>();

        ReadView.TypedListReadView<Block> blockList = view.getTypedListView("blocks", Registries.BLOCK.getCodec());
        ReadView.TypedListReadView<Integer> mappedChipsList = view.getTypedListView(
                "mappedChips", Codec.INT
        );

        this.blockMap = zipBlockMap(blockList, mappedChipsList).orElse(new HashMap<>());
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

        return Optional.of(new HashMap<>(blockIntegerMap));
    }

    public static class ChipsRenderData implements RenderData {
        private final Map<Block, Integer> blockMap;

        public ChipsRenderData(Map<Block, Integer> blockMap) {
            this.blockMap = blockMap;
        }

        public int getChips(Block block) {
            return blockMap.get(block);
        }

        @Override
        public void forEachKey(Consumer<Block> consumer) {
            blockMap.keySet().forEach(consumer);
        }
    }
}
