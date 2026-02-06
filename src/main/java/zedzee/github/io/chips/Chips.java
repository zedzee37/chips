package zedzee.github.io.chips;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.block.CornerInfo;
import zedzee.github.io.chips.block.entity.ChipsBlockEntities;
import zedzee.github.io.chips.block.entity.ChipsBlockEntity;
import zedzee.github.io.chips.component.ChipsComponents;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.item.ChipsBlockItem;
import zedzee.github.io.chips.item.ChipsItems;
import zedzee.github.io.chips.networking.BlockChipppedPayload;
import zedzee.github.io.chips.networking.ChipsBlockChangePayload;
import zedzee.github.io.chips.networking.ChiselAnimationPayload;

public class Chips implements ModInitializer {
    public static final String MOD_ID = "chips";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final RegistryKey<ItemGroup> CHIPS_ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(MOD_ID, "chips_item_group"));
    public static final ItemGroup CHIPS_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> ChipsBlockItem.getStack(Blocks.GRASS_BLOCK))
            .displayName(Text.translatable("itemGroup.chips_item_group"))
            .build();

    @Override
    public void onInitialize() {
        ChipsBlocks.init();
        ChipsBlockEntities.init();
        ChipsComponents.init();
        ChipsItems.init();

        PayloadTypeRegistry.playS2C().register(ChiselAnimationPayload.ID, ChiselAnimationPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ChipsBlockChangePayload.ID, ChipsBlockChangePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BlockChipppedPayload.ID, BlockChipppedPayload.PACKET_CODEC);

        Registry.register(Registries.ITEM_GROUP, CHIPS_ITEM_GROUP_KEY, CHIPS_ITEM_GROUP);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(itemGroup -> {
            itemGroup.add(ChipsItems.IRON_CHISEL.getDefaultStack());
//            itemGroup.add(ChipsItems.DIAMOND_CHISEL.getDefaultStack());
//            itemGroup.add(ChipsItems.NETHERITE_CHISEL.getDefaultStack());
        });

        ItemGroupEvents.modifyEntriesEvent(CHIPS_ITEM_GROUP_KEY).register(itemGroup -> {
                    itemGroup.add(ChipsItems.IRON_CHISEL.getDefaultStack());
//                    itemGroup.add(ChipsItems.DIAMOND_CHISEL.getDefaultStack());
//                    itemGroup.add(ChipsItems.NETHERITE_CHISEL.getDefaultStack());

                    Registries.BLOCK.stream().forEach(block -> {
                        if (!ChipsBlock.canBeChipped(block)) {
                            return;
                        }

                        ItemStack stack = ChipsBlockItem.getStack(block);
                        itemGroup.add(stack);
                    });
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(BlockChipppedPayload.ID,
                (payload, ctx) -> {
                    final World world = ctx.player().getWorld();
                    final BlockEntity maybeBlockEntity = world.getBlockEntity(payload.blockPos());

                    if (!(maybeBlockEntity instanceof final ChipsBlockEntity chipsBlockEntity)) {
                        return;
                    }

                    chipsBlockEntity.removeChips(payload.cornerInfo(), false);
                });
    }

    public static Identifier identifier(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
