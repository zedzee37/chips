package zedzee.github.io.chips;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerPickItemEvents;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zedzee.github.io.chips.block.ChipsBlockHelpers;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.item.ChipsItems;

public class Chips implements ModInitializer {
    public static final String MOD_ID = "chips";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ChipsBlocks.init();
        ChipsItems.init();

        PlayerPickItemEvents.BLOCK.register((player, pos, state, requestIncludeData) ->
            state.contains(ChipsBlockHelpers.CHIPS) ?
                    ChipsBlockHelpers.getStackWithChips(
                            state.getBlock().asItem().getDefaultStack(), state.get(ChipsBlockHelpers.CHIPS)
                    )
                    : null
        );
    }

    public static Identifier identifier(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
