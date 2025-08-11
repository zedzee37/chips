package zedzee.github.io.chips.screen;

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
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import zedzee.github.io.chips.block.ChipsBlockHelpers;
import zedzee.github.io.chips.item.ChipsItems;

public class ChiselingStationScreenHandler extends ScreenHandler implements ScreenHandlerListener {
    public static final ScreenHandlerType<ChiselingStationScreenHandler> CHISELING_STATION = register(
            "chiseling_station", ChiselingStationScreenHandler::new
    );

    private static <T extends ScreenHandler> ScreenHandlerType<T> register(String id, ScreenHandlerType.Factory<T> factory) {
        return Registry.register(Registries.SCREEN_HANDLER, id, new ScreenHandlerType<>(factory, FeatureFlags.VANILLA_FEATURES));
    }

    private final PlayerInventory playerInventory;
    private final Inventory craftingInventory;

    private static final int PATTERN_SLOT_IDX = 0;
    private static final int BLOCK_SLOT_IDX = 1;

    private final PatternSlot patternSlot;
    private final ChipsBlockSlot blockSlot;
    private final ResultSlot resultSlot;

    private final ScreenHandlerContext context;

    public ChiselingStationScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public ChiselingStationScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(CHISELING_STATION, syncId);
        this.context = context;
        this.playerInventory = playerInventory;
        this.craftingInventory = new SimpleInventory(3);
        this.patternSlot = new PatternSlot(craftingInventory, PATTERN_SLOT_IDX, 0, 0);
        this.blockSlot = new ChipsBlockSlot(craftingInventory, BLOCK_SLOT_IDX, 20,0);
        this.resultSlot = new ResultSlot(craftingInventory, 2, 40, 0);
        this.addSlots(playerInventory);
        this.addListener(this);
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
        ItemStack itemStack = ItemStack.EMPTY;

        if (slot2.hasStack()) {
            ItemStack itemStack2 = slot2.getStack();
            itemStack = itemStack2.copy();

            // 3 slots for our inventory
            if (slot < 3) {
                // 39 slots because 36 slots + 3
                if (!insertItem(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!insertItem(itemStack2, 0, 3, false)){
                return ItemStack.EMPTY;
            }

            slot2.onTakeItem(player, itemStack2);
        }

        return itemStack;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> this.dropInventory(player, this.craftingInventory));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    private void updateResult() {
        if (this.resultSlot.hasStack()) {
            return;
        }

        if (this.patternSlot.getStack().isOf(Items.PAPER) && this.blockSlot.hasStack()) {
            convertToPattern();
            return;
        }

        if (this.patternSlot.getStack().isOf(ChipsItems.CHIPS_PATTERN_ITEM) && this.blockSlot.hasStack()) {
            chiselItems();
        }
    }

    private void convertToPattern() {
        if (!this.patternSlot.getStack().isOf(Items.PAPER) ||
                !ChipsBlockHelpers.stackHasChips(this.blockSlot.getStack())) {
            return;
        }

        int chips = ChipsBlockHelpers.getChipsFromStack(this.blockSlot.getStack());
        this.resultSlot.setStack(ChipsBlockHelpers.getStackWithChips(ChipsItems.CHIPS_PATTERN_ITEM.getDefaultStack(), chips));
    }

    public void chiselItems() {
        if (!this.patternSlot.getStack().isOf(ChipsItems.CHIPS_PATTERN_ITEM) || !this.blockSlot.hasStack()) {
            return;
        }

        ItemStack pattern = this.patternSlot.getStack();
        int chips = ChipsBlockHelpers.getChipsFromStack(pattern);

        this.resultSlot.setStack(ChipsBlockHelpers.getStackWithChips(this.blockSlot.getStack(), chips));
    }

    @Override
    public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
        updateResult();
    }

    @Override
    public void onPropertyUpdate(ScreenHandler handler, int property, int value) {

    }

    static class ResultSlot extends Slot {
        public ResultSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            ItemStack patternStack = this.inventory.getStack(PATTERN_SLOT_IDX);
            if (patternStack.isOf(Items.PAPER)) {
                this.inventory.getStack(PATTERN_SLOT_IDX).decrement(1);
            } else if (patternStack.isOf(ChipsItems.CHIPS_PATTERN_ITEM)) {
                this.inventory.removeStack(BLOCK_SLOT_IDX);
            }
            super.onTakeItem(player, stack);
        }
    }

    static class ChipsBlockSlot extends Slot {
        public ChipsBlockSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return stack.getItem() instanceof BlockItem;
        }
    }

    static class PatternSlot extends Slot {
        public PatternSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return stack.isOf(Items.PAPER) || stack.isOf(ChipsItems.CHIPS_PATTERN_ITEM);
        }

        @Override
        public int getMaxItemCount() {
            return 1;
        }
    }
}
