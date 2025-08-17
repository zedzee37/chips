package zedzee.github.io.chips.block.entity;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChipsBlockEntity extends BlockEntity {
    private int chips = 255;
    private Map<Block, Integer> blockMap = Map.of(Blocks.AIR, 255);

    public ChipsBlockEntity(BlockPos pos, BlockState state) {
        super(ChipsBlockEntities.CHIPS_BLOCK_ENTITY, pos, state);
    }

    public int getChips() {
        return chips;
    }

    public void setChips(int chips) {
        this.chips = chips;
        markDirty();
    }

    @Override
    protected void writeData(WriteView view) {
        view.put("chips", Codec.INT, chips);

        WriteView.ListAppender<Block> blockListAppender = view.getListAppender("blocks", Block.CODEC.codec());
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
        this.chips = view.read("chips", Codec.INT).orElse(255);
        this.blockMap = new HashMap<>();

        ReadView.TypedListReadView<Block> blockList = view.getTypedListView("blocks", Block.CODEC.codec());
        ReadView.TypedListReadView<Integer> mappedChipsList = view.getTypedListView(
                "mappedChips", Codec.INT
        );

        this.blockMap = zipBlockMap(blockList, mappedChipsList).orElse(Map.of());

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
