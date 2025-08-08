package zedzee.github.io.chips.client.state;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import zedzee.github.io.chips.block.ChipsBlockHelpers;

public class ChipsPropertyHelpers {
    public static BlockState removeChipsProperty(BlockState target) {
        Reference2ObjectArrayMap<Property<?>, Comparable<?>> propertyMap = target.propertyMap.clone();
        propertyMap.remove(ChipsBlockHelpers.CHIPS);

        return new BlockState(target.getBlock(), propertyMap, target.codec);

    }
}
