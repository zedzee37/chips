package zedzee.github.io.chips.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.block.CornerInfo;
import zedzee.github.io.chips.render.RenderData;

import java.util.*;
import java.util.function.Consumer;

public class ChipsBlockEntity extends BlockEntity implements RenderDataBlockEntity {
    private Map<Block, BlockData> blockMap = new HashMap<>();
    private final Map<Block, BlockMetaData> blockMetaDataMap;

    private final static String NBT_BLOCK_DATA_CHIPS_KEY = "chips";
    private final static String NBT_BLOCK_DATA_DEFAULT_UV_KEY = "default_uv";
    private final static String NBT_BLOCKS_KEY = "blocks";
    private final static String NBT_BLOCK_DATA_KEY = "block_data";

    public ChipsBlockEntity(BlockPos pos, BlockState state) {
        super(ChipsBlockEntities.CHIPS_BLOCK_ENTITY, pos, state);
        blockMetaDataMap = new HashMap<>();
    }

    public CornerInfo getTotalChips() {
        CornerInfo total = CornerInfo.fromShape(0);
        for (Block block : blockMetaDataMap.keySet()) {
            total = total.union(blockMetaDataMap.get(block).getTotalChips());
        }
        return total;
    }

    public CornerInfo getChips(Block block) {
        if (blockMetaDataMap.containsKey(block)) {
            return blockMetaDataMap.get(block).getTotalChips();
        }
        return CornerInfo.EMPTY;
    }

    public void forEachKey(Consumer<Block> blockConsumer) {
        blockMetaDataMap.forEach((block, metaData) -> {
            blockConsumer.accept(block);
        });
    }

    // sets chips & syncs
    public void setChips(BlockState state, CornerInfo cornerInfo) {
        setChips(state, cornerInfo, true);
    }

    public void setChips(BlockState state, CornerInfo corner, boolean sync) {
        BlockMetaData metaData = blockMetaDataMap.getOrDefault(state.getBlock(), new BlockMetaData(false));
        metaData.setChips(state, corner);

        if (sync) sync();
    }

    @Nullable
    public Block firstBlockWithCorner(CornerInfo corner) {
        for (Block block : blockMetaDataMap.keySet()) {
            BlockMetaData blockMetaData = blockMetaDataMap.get(block);
            CornerInfo totalChips = blockMetaData.getTotalChips();

            if (totalChips.hasShape(corner)) {
                return block;
            }
        }
        return null;
    }

    public void toggleDefaultUv(Block block) {
        if (!blockMetaDataMap.containsKey(block)) {
            return;
        }

        BlockMetaData metaData = blockMetaDataMap.get(block);
        metaData.toggleDefaultUv();

        this.markDirty();
        sync();
    }

    public void clear() {
        this.blockMetaDataMap.clear();
        markDirty();
        sync();
    }

    @Override
    public @Nullable Object getRenderData() {
        // it must use a copy to prevent mutation
        return new ChipsRenderData(Map.copyOf(blockMap));
    }

    public boolean hasCorner(CornerInfo corner) {
        return getTotalChips().hasShape(corner);
    }

    public void addChips(BlockState state, CornerInfo corner) {
        BlockMetaData metaData = blockMetaDataMap.getOrDefault(state.getBlock(), new BlockMetaData(false));
        metaData.addChips(state, corner);

        markDirty();
        sync();
    }

    public List<BlockState> removeChips(CornerInfo corner) {
        return removeChips(corner, true);
    }

    public List<BlockState> removeChips(CornerInfo corner, boolean sync) {
        List<Block> removedChips = new ArrayList<>();
        List<Block> destroyedChips = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            int targetCorner = 1 << i;
            if ((targetCorner & corner.shape()) == 0) {
                continue;
            }

            forEachKey(block -> {
                int blockChips = getChips(block);
                if ((blockChips & targetCorner) == 0) {
                    return;
                }

                removeChips(block, cornerInfo, false, false);

                if (getChips(block) == 0) {
                    destroyedChips.add(block);
                }

                removedChips.add(block);
            });
        }

        destroyedChips.forEach(block -> blockMap.remove(block));

        if (sync) sync();
        return removedChips;
    }

    public void removeChips(Block block, CornerInfo cornerInfo, boolean sync, boolean remove) {
        if (!blockMap.containsKey(block)) {
            return;
        }

        int currentChips = blockMap.get(block).getChips();
        int newChips = currentChips & (~cornerInfo.shape());

        if (newChips == 0 && remove) {
            blockMap.remove(block);
        } else {
            setChips(block, newChips, false);
        }

        if (getTotalChips() == 0) {
            world.removeBlock(pos, false);
        }

        if (sync) {
            sync();
        }
    }

    public @Nullable BlockState getStateAtCorner(CornerInfo corner) {
        for (BlockMetaData metaData : blockMetaDataMap.values()) {
            for (BlockState state : metaData.blockStates()) {
                if (metaData.getChips(state).hasShape(corner)) {
                    return state;
                }
            }
        }
        return null;
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public void sync() {
        markDirty();
        if (world == null) return;
        calculateLighting();
        world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL_AND_REDRAW);
    }

    private void calculateLighting() {
        if (world.getBlockState(getPos()).contains(ChipsBlock.LIGHT_LEVEL)) {
            float totalLuminance = 0;
            for (Block curBlock : blockMap.keySet()) {
                int corners = ChipsBlock.countCorners(getChips(curBlock));
                BlockState defaultState = curBlock.getDefaultState();
                int luminance = defaultState.getLuminance() * corners;
                totalLuminance += luminance / 8.0f;
            }
            totalLuminance = Math.min(15, Math.round(totalLuminance));

            world.setBlockState(getPos(), world.getBlockState(getPos()).with(ChipsBlock.LIGHT_LEVEL, (int)totalLuminance), Block.NOTIFY_ALL);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList blockList = new NbtList();
        NbtList blockElementList = new NbtList();

        blockMap.forEach((block, blockData) -> {
            blockList.add(NbtString.of(Registries.BLOCK.getId(block).toString()));

            NbtCompound dataCompound = new NbtCompound();
            dataCompound.putInt(NBT_BLOCK_DATA_CHIPS_KEY, blockData.getChips());
            dataCompound.putBoolean(NBT_BLOCK_DATA_DEFAULT_UV_KEY, blockData.shouldUseDefaultUv());
            blockElementList.add(dataCompound);
        });
        nbt.put(NBT_BLOCKS_KEY, blockList);
        nbt.put(NBT_BLOCK_DATA_KEY, blockElementList);
        super.writeNbt(nbt, registryLookup);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (!nbt.contains(NBT_BLOCKS_KEY) || !nbt.contains(NBT_BLOCK_DATA_KEY)) {
            super.readNbt(nbt, registryLookup);
            return;
        }

        NbtList blockList = nbt.getList(NBT_BLOCKS_KEY, NbtElement.STRING_TYPE);
        NbtList blockDataList = nbt.getList(NBT_BLOCK_DATA_KEY, NbtElement.COMPOUND_TYPE);
        assert blockList.size() == blockDataList.size();

        blockMap.clear();
        for (int i = 0; i < blockList.size(); i++) {
            String id = blockList.getString(i);
            Block block = Registries.BLOCK.get(Identifier.of(id));

            NbtCompound compound = blockDataList.getCompound(i);
            int chips = compound.getInt(NBT_BLOCK_DATA_CHIPS_KEY);
            boolean shouldUseDefaultUv = compound.getBoolean(NBT_BLOCK_DATA_DEFAULT_UV_KEY);
            BlockData blockData = new BlockData(chips, shouldUseDefaultUv);

            blockMap.put(block, blockData);
        }

        super.readNbt(nbt, registryLookup);
    }

    public static class ChipsRenderData implements RenderData {
        private final Map<Block, BlockData> blockMap;

        public ChipsRenderData(Map<Block, BlockData> blockMap) {
            this.blockMap = blockMap;
        }

        public int getChips(Block block) {
            return blockMap.get(block).getChips();
        }

        public Set<Block> getBlocks() {
            return blockMap.keySet();
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

    public static class BlockMetaData {
        private final Map<BlockState, Byte> blockStateMap;
        private boolean defaultUv;

        public BlockMetaData(boolean defaultUv) {
            this.defaultUv = defaultUv;
            this.blockStateMap = new HashMap<>();
        }

        public boolean hasDefaultUv() {
            return this.defaultUv;
        }

        public void setDefaultUv(boolean defaultUv) {
            this.defaultUv = defaultUv;
        }

        public void toggleDefaultUv() {
            setDefaultUv(!hasDefaultUv());
        }

        public CornerInfo getTotalChips() {
            int total = 0;
            for (Byte shape : blockStateMap.values()) {
                total |= shape;
            }
            return CornerInfo.fromShape(total);
        }

        public void setChips(BlockState state, CornerInfo corner) {
            blockStateMap.put(state, (byte)corner.shape());
        }

        public void addChips(BlockState state, CornerInfo corner) {
            byte currentChips = blockStateMap.getOrDefault(state, (byte)0);
            currentChips |= (byte)corner.shape();
            setChips(state, CornerInfo.fromShape(currentChips));
        }

        public CornerInfo getChips(BlockState state) {
            return CornerInfo.fromShape(blockStateMap.getOrDefault(state, (byte)0));
        }

        public Set<BlockState> blockStates() {
            return blockStateMap.keySet();
        }
    }
}
