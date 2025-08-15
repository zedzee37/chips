package zedzee.github.io.chips;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerPickItemEvents;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zedzee.github.io.chips.util.ChipsBlockHelpers;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.item.ChipsItems;
import zedzee.github.io.chips.util.ChipsItemHelpers;

public class Chips implements ModInitializer {
    public static final String MOD_ID = "chips";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ChipsBlocks.init();
        ChipsItems.init();

        PlayerPickItemEvents.BLOCK.register((player, pos, state, requestIncludeData) ->
            state.contains(ChipsBlockHelpers.CHIPS) ?
                    ChipsItemHelpers.getStackWithChipsCopy(
                            state.getBlock().asItem().getDefaultStack(), state.get(ChipsBlockHelpers.CHIPS)
                    )
                    : null
        );

        RegistryEntryAddedCallback.event(Registries.BLOCK).register((rawID, listener, block) -> {
            BlockState state = block.getDefaultState();

            if (state.contains(ChipsBlockHelpers.CHIPS)) {
                block.setDefaultState(state.with(ChipsBlockHelpers.CHIPS, 255));
            }

            if (state.contains(ChipsBlockHelpers.FACING)) {
                block.setDefaultState(state.with(ChipsBlockHelpers.FACING, Direction.NORTH));
            }
        });
    }

    public static Identifier identifier(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
