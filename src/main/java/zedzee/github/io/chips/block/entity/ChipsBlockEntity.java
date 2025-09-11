package zedzee.github.io.chips.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.block.CornerInfo;
import zedzee.github.io.chips.render.RenderData;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChipsBlockEntity extends BlockEntity implements RenderDataBlockEntity {
    private Map<Block, BlockData> blockMap = new HashMap<>();

    public ChipsBlockEntity(BlockPos pos, BlockState state) {
        super(ChipsBlockEntities.CHIPS_BLOCK_ENTITY, pos, state);
    }

    public int getTotalChips() {
        int total = 0;
        for (BlockData value : blockMap.values()) {
            total |= value.getChips();
        }
        return total;
    }

    public int getChips(Block block) {
        return this.blockMap.get(block).getChips();
    }

    public void forEachKey(Consumer<Block> blockConsumer) {
        blockMap.keySet().forEach(blockConsumer);
    }

    public void setChips(Block block, int chips) {
        if (this.blockMap.containsKey(block)) {
            BlockData data = this.blockMap.get(block);
            data.setChips(chips);
        } else {
            this.blockMap.put(block, new BlockData(chips));
        }

        markDirty();
        sync();
    }

    @Nullable
    public Block firstBlockWithCorner(CornerInfo corner) {
        for (Block block : blockMap.keySet()) {
            int chips = this.getChips(block);
            if ((chips & corner.shape()) != 0) {
                return block;
            }
        }
        return null;
    }

    public boolean hasBlock(Block block) {
        return blockMap.containsKey(block);
    }

    public void toggleDefaultUv(Block block) {
        if (!this.blockMap.containsKey(block)) {
            return;
        }

        BlockData data = this.blockMap.get(block);
        data.setDefaultUv(!data.shouldUseDefaultUv());
        this.markDirty();
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
            currentChips = blockMap.get(block).getChips();
        }

        int newChips = currentChips | chips;
        setChips(block, newChips);
    }

    public List<Block> removeChips(int chips) {
        List<Block> removedChips = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            int targetCorner = 1 << i;
            if ((targetCorner & chips) == 0) {
                continue;
            }

            forEachKey(block -> {
                int blockChips = getChips(block);
                if ((blockChips & targetCorner) == 0) {
                    return;
                }

                removeChips(block, targetCorner);
                removedChips.add(block);
            });
        }
        return removedChips;
    }

    public void removeChips(Block block, int chips) {
        if (!blockMap.containsKey(block)) {
            return;
        }

        int currentChips = blockMap.get(block).getChips();
        int newChips = currentChips & (~chips);
        setChips(block, newChips);
    }

    public @Nullable Block getBlockAtCorner(int corner) {
        for (Block key : blockMap.keySet()) {
            int chips = getChips(key);
            if ((chips & corner) != 0) {
                return key;
            }
        }

        return null;
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

//    @Override
//    public @Nullable Object getRenderData() {
//        return blockMap;
//    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public void sync() {
        if (world == null) {
            return;
        }

        if (world.getBlockState(pos).contains(ChipsBlock.LIGHT_LEVEL)) {
            float totalLuminance = 0;
            for (Block curBlock : blockMap.keySet()) {
                int corners = ChipsBlock.countCorners(getChips(curBlock));
                BlockState defaultState = curBlock.getDefaultState();
                int luminance = defaultState.getLuminance() * corners;
                totalLuminance += luminance / 8.0f;
            }
            totalLuminance = Math.min(15, Math.round(totalLuminance));

            world.setBlockState(pos, world.getBlockState(pos).with(ChipsBlock.LIGHT_LEVEL, (int)totalLuminance), Block.NOTIFY_ALL);
        }
        // calculate lighthing changes

        world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);

        if (!world.isClient) {
            markDirty();
            ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
        }
    }

    @Override
    protected void writeData(WriteView view) {
        WriteView.ListAppender<Block> blockListAppender = view.getListAppender("blocks", Registries.BLOCK.getCodec());
        WriteView.ListAppender<BlockData> mappedChipsListAppender = view.getListAppender(
                "mappedChips", BlockData.CODEC
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
        ReadView.TypedListReadView<BlockData> mappedChipsList = view.getTypedListView(
                "mappedChips", BlockData.CODEC
        );

        this.blockMap = zipBlockMap(blockList, mappedChipsList).orElse(new HashMap<>());
        sync();
        super.readData(view);
    }

    private Optional<Map<Block, BlockData>> zipBlockMap(
            ReadView.TypedListReadView<Block> blockList,
            ReadView.TypedListReadView<BlockData> mappedChipsList) {
        List<Block> blocks = blockList.stream().toList();
        List<BlockData> mappedChips = mappedChipsList.stream().toList();

        if (blocks.size() != mappedChips.size()) {
            return Optional.empty();
        }
        Map<Block, BlockData> blockIntegerMap = IntStream.range(0, blocks.size())
                .boxed()
                .collect(Collectors.toMap(
                        blocks::get,
                        mappedChips::get
                ));

        return Optional.of(new HashMap<>(blockIntegerMap));
    }

    public static class ChipsRenderData implements RenderData {
        private final Map<Block, BlockData> blockMap;

        public ChipsRenderData(Map<Block, BlockData> blockMap) {
            this.blockMap = blockMap;
        }

        public int getChips(Block block) {
            return blockMap.get(block).getChips();
        }

        @Override
        public void forEachBlock(Consumer<Block> consumer) {
            blockMap.keySet().forEach(consumer);
        }

        @Override
        public boolean shouldUseDefaultUv(Block block) {
            return blockMap.get(block).shouldUseDefaultUv();
        }
    }

    public static class BlockData {
        public static final Codec<BlockData> CODEC = RecordCodecBuilder.create(
                builder -> builder.group(
                        Codec.INT.fieldOf("chips_value").forGetter(BlockData::getChips),
                        Codec.BOOL.fieldOf("should_use_default_uv").forGetter(BlockData::shouldUseDefaultUv)
                ).apply(builder, BlockData::new)
        );

        private int chips;
        private boolean defaultUv;

        private BlockData(int chips, boolean defaultUv) {
            this.chips = chips;
            this.defaultUv = defaultUv;
        }

        public BlockData(int chips) {
            this.chips = chips;
            this.defaultUv = false;
        }

        public void setDefaultUv(boolean to) {
            this.defaultUv = to;
        }

        public boolean shouldUseDefaultUv() {
            return this.defaultUv;
        }

        public void setChips(int chips) {
            this.chips = chips;
        }

        public int getChips() {
            return this.chips;
        }
    }
}
