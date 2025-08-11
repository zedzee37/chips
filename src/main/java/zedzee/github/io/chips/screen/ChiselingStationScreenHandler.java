package zedzee.github.io.chips.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class ChiselingStationScreenHandler extends ScreenHandler {
    public static final ScreenHandlerType<ChiselingStationScreenHandler> CHISELING_STATION = register(
            "chiseling_station", ChiselingStationScreenHandler::new
    );

    private static <T extends ScreenHandler> ScreenHandlerType<T> register(String id, ScreenHandlerType.Factory<T> factory) {
        return Registry.register(Registries.SCREEN_HANDLER, id, new ScreenHandlerType<>(factory, FeatureFlags.VANILLA_FEATURES));
    }

    private final Inventory patternInventory;
    private final PatternSlot patternSlot;

    public ChiselingStationScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(CHISELING_STATION, syncId);
        this.patternInventory = new SimpleInventory(1);
        this.patternSlot = new PatternSlot(patternInventory, 0, 0, 0);
        this.addSlots(playerInventory);
    }

    private void addSlots(PlayerInventory playerInventory) {
        this.addSlot(patternSlot);
        addPlayerSlots(playerInventory, 8, 84);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    static class PatternSlot extends Slot {
        public PatternSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return stack.isOf(Items.PAPER);
        }
    }
}
