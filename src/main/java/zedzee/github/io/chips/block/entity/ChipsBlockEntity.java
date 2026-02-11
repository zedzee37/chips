package zedzee.github.io.chips.block.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.*;
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
    private final Map<BlockState, ChipData> stateMap = new HashMap<>();

    private final static String NBT_BLOCK_DATA_CHIPS_KEY = "chips";
    private final static String NBT_BLOCK_DATA_DEFAULT_UV_KEY = "default_uv";
    private final static String NBT_BLOCKS_KEY = "blocks";
    private final static String NBT_BLOCK_DATA_KEY = "block_data";

    public ChipsBlockEntity(BlockPos pos, BlockState state) {
        super(ChipsBlockEntities.CHIPS_BLOCK_ENTITY, pos, state);
    }

    public CornerInfo getTotalChips() {
        CornerInfo total = CornerInfo.fromShape(0);

        for (ChipData chipData : stateMap.values()) {
            total = total.union(chipData.getShape());
        }

        return total;
    }

    public CornerInfo getChips(BlockState state) {
        if (stateMap.containsKey(state)) {
            return stateMap.get(state).getShape();
        }

        return CornerInfo.EMPTY;
    }

    public void forEachKey(Consumer<BlockState> blockConsumer) {
        stateMap.keySet().forEach(blockConsumer);
    }

    // sets chips & syncs
    public void setChips(BlockState state, CornerInfo cornerInfo) {
        setChips(state, cornerInfo, true);
    }

    public void setChips(BlockState state, CornerInfo corner, boolean sync) {
        ChipData data = stateMap.getOrDefault(state, new ChipData());
        data.setShape(corner);
        stateMap.put(state, data);

        if (sync) sync();
    }

    @Nullable
    public BlockState firstBlockWithCorner(CornerInfo corner) {
        for (BlockState state : stateMap.keySet()) {
            ChipData data = stateMap.get(state);
            if (data.getShape().hasShape(corner)) {
                return state;
            }
        }

        return null;
    }

    public void toggleDefaultUv(BlockState state) {
        if (!stateMap.containsKey(state)) {
            return;
        }

        stateMap.get(state).toggleDefaultUv();
        this.markDirty();
        sync();
    }

    public void clear() {
        stateMap.clear();
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

    public void addChips(BlockState state, CornerInfo shape) {
        ChipData data = stateMap.getOrDefault(state, new ChipData());
        data.union(shape);
        stateMap.put(state, data);

        markDirty();
        sync();
    }

    public List<BlockState> removeChips(CornerInfo corner) {
        return removeChips(corner, true);
    }

    public List<BlockState> removeChips(CornerInfo corner, boolean sync) {
        List<BlockState> removedChips = new ArrayList<>();
        List<BlockState> destroyedChips = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            CornerInfo targetCorner = CornerInfo.fromShape(1 << i);
            if (!corner.hasShape(targetCorner)) {
                continue;
            }

            forEachKey(state -> {
                CornerInfo shape = getChips(state);
                if (!shape.hasShape(corner)) {
                    return;
                }

                removeChips(state, corner, false, false);

                if (getChips(state).isEmpty()) {
                    destroyedChips.add(state);
                }

                removedChips.add(state);
            });
        }

        destroyedChips.forEach(stateMap::remove);
        if (sync) sync();
        return removedChips;
    }

    public void removeChips(BlockState state, CornerInfo corner, boolean sync, boolean remove) {
        if (!stateMap.containsKey(state)) {
            return;
        }

        CornerInfo currentChips = stateMap.get(state).getShape();
        CornerInfo newChips = currentChips.removeShape(corner);

        if (newChips.isEmpty() && remove) {
            stateMap.remove(state);
        } else {
            setChips(state, newChips, false);
        }

        if (getTotalChips().isEmpty()) {
            world.removeBlock(pos, false);
        }

        if (sync) {
            sync();
        }
    }

    public @Nullable BlockState getStateAtCorner(CornerInfo corner) {
        for (BlockState state : stateMap.keySet()) {
            ChipData data = stateMap.get(state);
            if (data.getShape().hasShape(corner)) {
                return state;
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
            for (BlockState state : stateMap.keySet()) {
                int corners = ChipsBlock.countCorners(getChips(state).shape());
                int luminance = state.getLuminance() * corners;
                totalLuminance += luminance / 8.0f;
            }
            totalLuminance = Math.min(15, Math.round(totalLuminance));

            world.setBlockState(getPos(), world.getBlockState(getPos()).with(ChipsBlock.LIGHT_LEVEL, (int)totalLuminance), Block.NOTIFY_ALL);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList blockStateList = new NbtList();
        NbtList blockElementList = new NbtList();

        stateMap.forEach((state, blockData) -> {
            DataResult<NbtElement> maybeResult = BlockState.CODEC.encodeStart(NbtOps.INSTANCE, state);

            if (!maybeResult.hasResultOrPartial() && maybeResult.result().isEmpty()) {
                return;
            }

            NbtElement encodedState = maybeResult.result().get();
            blockStateList.add(encodedState);

            ChipData data = stateMap.get(state);

            NbtCompound dataCompound = new NbtCompound();
            dataCompound.putInt(NBT_BLOCK_DATA_CHIPS_KEY, data.getShape().shape());
            dataCompound.putBoolean(NBT_BLOCK_DATA_DEFAULT_UV_KEY, data.hasDefaultUv());
            blockElementList.add(dataCompound);
        });
        nbt.put(NBT_BLOCKS_KEY, blockStateList);
        nbt.put(NBT_BLOCK_DATA_KEY, blockElementList);
        super.writeNbt(nbt, registryLookup);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (!nbt.contains(NBT_BLOCKS_KEY) || !nbt.contains(NBT_BLOCK_DATA_KEY)) {
            super.readNbt(nbt, registryLookup);
            return;
        }

        NbtList blockList = nbt.getList(NBT_BLOCKS_KEY, NbtElement.COMPOUND_TYPE);
        NbtList blockDataList = nbt.getList(NBT_BLOCK_DATA_KEY, NbtElement.COMPOUND_TYPE);
        assert blockList.size() == blockDataList.size();

        blockMap.clear();
        for (int i = 0; i < blockList.size(); i++) {
            NbtElement element = blockList.get(i);
            DataResult<Pair<BlockState, NbtElement>> maybeState = BlockState.CODEC.decode(NbtOps.INSTANCE, element);

            if (!maybeState.hasResultOrPartial() || maybeState.result().isEmpty()) {
                return;
            }

            Pair<BlockState, NbtElement> statePair = maybeState.getOrThrow();
            BlockState state = statePair.getFirst();

            NbtCompound compound = blockDataList.getCompound(i);
            int shape = compound.getInt(NBT_BLOCK_DATA_CHIPS_KEY);
            boolean shouldUseDefaultUv = compound.getBoolean(NBT_BLOCK_DATA_DEFAULT_UV_KEY);
            ChipData data = new ChipData(CornerInfo.fromShape(shape), shouldUseDefaultUv);

            stateMap.put(state, data);
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

    public static class ChipData {
        private CornerInfo shape;
        private boolean defaultUv;

        public ChipData(CornerInfo shape, boolean defaultUv) {
            this.shape = shape;
            this.defaultUv = defaultUv;
        }

        public ChipData(CornerInfo shape) {
            this(shape, false);
        }

        public ChipData() {
            this(CornerInfo.fromIndex(0));
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

        public CornerInfo getShape() {
            return this.shape;
        }

        public void setShape(CornerInfo shape) {
            this.shape = shape;
        }

        public void union(CornerInfo otherShape) {
            this.shape = shape.union(otherShape);
        }
    }
}
