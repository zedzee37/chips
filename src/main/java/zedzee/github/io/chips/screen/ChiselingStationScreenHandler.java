package zedzee.github.io.chips.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class ChiselingStationScreenHandler extends ScreenHandler {
    public static final ScreenHandlerType<ChiselingStationScreenHandler> CHISELING_STATION = register(
            "chiseling_station", ChiselingStationScreenHandler::new
    );

    private static <T extends ScreenHandler> ScreenHandlerType<T> register(String id, ScreenHandlerType.Factory<T> factory) {
        return Registry.register(Registries.SCREEN_HANDLER, id, new ScreenHandlerType<>(factory, FeatureFlags.VANILLA_FEATURES));
    }

    public ChiselingStationScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(CHISELING_STATION, syncId);
        this.addSlots(playerInventory);
    }

    private void addSlots(PlayerInventory playerInventory) {
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
}
