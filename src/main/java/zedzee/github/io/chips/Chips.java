package zedzee.github.io.chips;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerPickItemEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zedzee.github.io.chips.block.ChipsBlock;
import zedzee.github.io.chips.block.entity.ChipsBlockEntities;
import zedzee.github.io.chips.block.entity.ChipsBlockEntity;
import zedzee.github.io.chips.component.ChipsComponents;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.item.ChipsBlockItem;
import zedzee.github.io.chips.item.ChipsItems;
import zedzee.github.io.chips.mixin.PreparedRecipesAccessor;
import zedzee.github.io.chips.networking.ChipsBlockChangePayload;
import zedzee.github.io.chips.networking.ChiselAnimationPayload;

public class Chips implements ModInitializer {
    public static final String MOD_ID = "chips";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ChipsBlocks.init();
        ChipsBlockEntities.init();
        ChipsComponents.init();
        ChipsItems.init();

        PayloadTypeRegistry.playS2C().register(ChiselAnimationPayload.ID, ChiselAnimationPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ChipsBlockChangePayload.ID, ChipsBlockChangePayload.CODEC);

        // maybe this for custom crafting?
//        ServerTickEvents.START_SERVER_TICK.register((server) -> {
//            ServerRecipeManager recipeManager = server.getRecipeManager();
//            PreparedRecipesAccessor accessor = (PreparedRecipesAccessor) recipeManager;
//
//            PreparedRecipes recipes = accessor.getPreparedRecipes();
//
//            recipes.recipes().add(new RecipeEntry<CraftingRecipe>(new ShapedRecipe(
//                    "",
//                    CraftingRecipeCategory.MISC,
//                    new RawShapedRecipe(3, 3)
//            )));
//        });

        PlayerPickItemEvents.BLOCK.register(
                (player, pos, state, requestIncludeData) -> {
                    if (!state.isOf(ChipsBlocks.CHIPS_BLOCK)) {
                        return null;
                    }

                    World world = player.getWorld();
                    BlockEntity be = world.getBlockEntity(pos);

                    if (!(be instanceof ChipsBlockEntity chipsBlockEntity)) {
                        return null;
                    }

                    int hoveredCorner = ChipsBlock.getHoveredCorner(world, player);
                    if (hoveredCorner == -1) {
                        return null;
                    }

                    Block block = chipsBlockEntity.getBlockAtCorner(1 << hoveredCorner);

                    if (block == null) {
                        return null;
                    }

                    return ChipsBlockItem.getStack(block);
        });
    }

    public static Identifier identifier(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
