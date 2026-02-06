package zedzee.github.io.chips.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.block.CornerInfo;
import zedzee.github.io.chips.render.RenderData;

import javax.print.DocFlavor;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChipsBlockEntity extends BlockEntity implements RenderDataBlockEntity {
    private Map<Block, BlockData> blockMap = new HashMap<>();

    private final static String NBT_BLOCK_DATA_CHIPS_KEY = "chips";
    private final static String NBT_BLOCK_DATA_DEFAULT_UV_KEY = "default_uv";
    private final static String NBT_BLOCKS_KEY = "blocks";
    private final static String NBT_BLOCK_DATA_KEY = "block_data";

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

    // sets chips & syncs
    public void setChips(Block block, int chips) {
        setChips(block, chips, true);
    }

    public void setChips(Block block, int chips, boolean sync) {
        if (this.blockMap.containsKey(block)) {
            BlockData data = this.blockMap.get(block);
            data.setChips(chips);
        } else if (chips != 0) {
            this.blockMap.put(block, new BlockData(chips));
        }

        if (sync) sync();
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
        // it must use a copy to prevent mutation
        return new ChipsRenderData(Map.copyOf(blockMap));
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

    public List<Block> removeChips(CornerInfo cornerInfo) {
        return removeChips(cornerInfo, true);
    }

    public List<Block> removeChips(CornerInfo cornerInfo, boolean sync) {
        List<Block> removedChips = new ArrayList<>();
        List<Block> destroyedChips = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            int targetCorner = 1 << i;
            if ((targetCorner & cornerInfo.shape()) == 0) {
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

    public @Nullable Block getBlockAtCorner(CornerInfo cornerInfo) {
        for (Block key : blockMap.keySet()) {
            int chips = getChips(key);
            if ((chips & cornerInfo.shape()) != 0) {
                return key;
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
}
