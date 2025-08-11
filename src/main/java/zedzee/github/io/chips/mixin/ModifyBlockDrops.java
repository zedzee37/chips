package zedzee.github.io.chips.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootWorldContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.ChipsBlockHelpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(AbstractBlock.class)
public class ModifyBlockDrops {
    @Inject(method = "getDroppedStacks", at = @At("RETURN"), cancellable = true)
    private static void addBlockStateComponent(
            BlockState state,
            LootWorldContext.Builder builder,
            CallbackInfoReturnable<List<ItemStack>> cir
    ) {
        if (!state.contains(ChipsBlockHelpers.CHIPS)) {
            return;
        }

        int chips = state.get(ChipsBlockHelpers.CHIPS);
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put(ChipsBlockHelpers.CHIPS.getName(), String.valueOf(chips));

        if (cir == null) {
            Chips.LOGGER.info("????");
            return;
        }
        List<ItemStack> drops = cir.getReturnValue();

        drops
                .stream()
                .filter(stack ->
                        stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock().equals(state.getBlock())
                )
                .forEach(stack ->
                    stack.set(DataComponentTypes.BLOCK_STATE, new BlockStateComponent(propertyMap))
                );

        cir.setReturnValue(drops);
    }
}
