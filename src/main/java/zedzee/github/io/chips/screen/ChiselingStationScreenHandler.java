package zedzee.github.io.chips.screen;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.ChipsBlockHelpers;

public class ChiselingStationScreenHandler extends ScreenHandler {
    public static final ScreenHandlerType<ChiselingStationScreenHandler> CHISELING_STATION = register(
            "chiseling_station", ChiselingStationScreenHandler::new
    );

    private static <T extends ScreenHandler> ScreenHandlerType<T> register(String id, ScreenHandlerType.Factory<T> factory) {
        return Registry.register(Registries.SCREEN_HANDLER, id, new ScreenHandlerType<>(factory, FeatureFlags.VANILLA_FEATURES));
    }

    private final Inventory craftingInventory;
    private final PatternSlot patternSlot;
    private final ChipsBlockSlot blockSlot;
    private final ResultSlot resultSlot;

    public ChiselingStationScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(CHISELING_STATION, syncId);
        this.craftingInventory = new SimpleInventory(3);
        this.patternSlot = new PatternSlot(craftingInventory, 0, 0, 0);
        this.blockSlot = new ChipsBlockSlot(craftingInventory, 1, 20,0);
        this.resultSlot = new ResultSlot(craftingInventory, 2, 40, 0);
        this.addSlots(playerInventory);
    }

    private void addSlots(PlayerInventory playerInventory) {
        this.addSlot(patternSlot);
        this.addSlot(blockSlot);
        this.addSlot(resultSlot);
        addPlayerSlots(playerInventory, 8, 84);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        Slot slot2 = this.slots.get(slot);
        Chips.LOGGER.info(slot2.inventory.getStack(slot).getName().toString());

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    static class ResultSlot extends Slot {
        public ResultSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }
    }

    static class ChipsBlockSlot extends Slot {
        public ChipsBlockSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            if (stack.getItem() instanceof BlockItem || !stack.contains(DataComponentTypes.BLOCK_STATE)) {
                return false;
            }

            BlockStateComponent component = stack.get(DataComponentTypes.BLOCK_STATE);

            return
                    component != null &&
                    component.properties().containsKey(ChipsBlockHelpers.CHIPS.getName());
        }
    }

    static class PatternSlot extends Slot {
        public PatternSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return stack.isOf(Items.PAPER);
        }

        @Override
        public int getMaxItemCount() {
            return 1;
        }
    }
}
