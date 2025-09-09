package zedzee.github.io.chips.item;

import net.fabricmc.fabric.impl.recipe.ingredient.builtin.ComponentsIngredient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentChanges;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.ChipsBlocks;
import zedzee.github.io.chips.block.entity.ChipsBlockEntity;
import zedzee.github.io.chips.component.ChipsBlockItemComponent;
import zedzee.github.io.chips.component.ChipsComponents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ChipsBlockItem extends BlockItem {
    private final static float EPSILON = 0.01f;

    public ChipsBlockItem(Settings settings) {
        super(ChipsBlocks.CHIPS_BLOCK, settings);
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack defaultStack = super.getDefaultStack();
        defaultStack.set(ChipsComponents.BLOCK_COMPONENT_COMPONENT, new ChipsBlockItemComponent(Blocks.DIAMOND_BLOCK));
        return defaultStack;
    }

    public static ItemStack getStack(Block block) {
        ItemStack stack = ChipsItems.CHIPS_BLOCK_ITEM.getDefaultStack().copy();
        stack.set(ChipsComponents.BLOCK_COMPONENT_COMPONENT, new ChipsBlockItemComponent(block));
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
        if (stack.contains(ChipsComponents.BLOCK_COMPONENT_COMPONENT)) {
            MinecraftServer server = world.getServer();
            ServerRecipeManager serverRecipeManager = server.getRecipeManager();

            Block blockType = stack.get(ChipsComponents.BLOCK_COMPONENT_COMPONENT).block();

            RegistryKey<Recipe<?>> recipeRegistryKey = getRecipeKey(blockType);
            if (serverRecipeManager.get(recipeRegistryKey).isEmpty()) {
                ArrayList<RecipeEntry<?>> recipeEntries = new ArrayList<>(serverRecipeManager.values());
                recipeEntries.add(getRecipeEntry(recipeRegistryKey, blockType));
                serverRecipeManager.preparedRecipes = PreparedRecipes.of(recipeEntries);
            }
        }

        super.inventoryTick(stack, world, entity, slot);
    }

    private RegistryKey<Recipe<?>> getRecipeKey(Block block) {
        Identifier identifier = Chips.identifier("chips_item_" + Registries.BLOCK.getId(block).getPath());
        return RegistryKey.of(RegistryKeys.RECIPE, identifier);
    }

    private RecipeEntry<?> getRecipeEntry(RegistryKey<Recipe<?>> key, Block block) {
        RawShapedRecipe recipe = new RawShapedRecipe(3, 3, getIngredients(block), Optional.empty());

        return new RecipeEntry<CraftingRecipe>(
                key,
                new ShapedRecipe(
                        "",
                        CraftingRecipeCategory.MISC,
                        recipe,
                        block.asItem().getDefaultStack()
                )
        );
    }

    private List<Optional<Ingredient>> getIngredients(Block block) {
        Ingredient baseIngredient = Ingredient.ofItem(ChipsItems.CHIPS_BLOCK_ITEM);
        ComponentChanges.Builder builder = ComponentChanges.builder();
        builder.add(new Component<>(ChipsComponents.BLOCK_COMPONENT_COMPONENT, new ChipsBlockItemComponent(block)));

        Optional<Ingredient> componentsIngredient = Optional.of(
                new ComponentsIngredient(baseIngredient, builder.build())
                        .toVanilla()
        );
        Optional<Ingredient> slimeBallIngredient = Optional.of(Ingredient.ofItem(Items.SLIME_BALL));
        ArrayList<Optional<Ingredient>> ingredients = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            ingredients.add(componentsIngredient);
        }

        ingredients.add(componentsIngredient);
        ingredients.add(slimeBallIngredient);
        ingredients.add(componentsIngredient);

        for (int i = 0; i < 3; i++) {
            ingredients.add(componentsIngredient);
        }

        return ingredients;
    }

    @Override
    public ActionResult place(ItemPlacementContext context) {
        ItemStack stack = context.getStack();
        if (!stack.contains(ChipsComponents.BLOCK_COMPONENT_COMPONENT)) {
            return ActionResult.FAIL;
        }

        Block blockType = stack.get(ChipsComponents.BLOCK_COMPONENT_COMPONENT).block();

        World world = context.getWorld();
        Vec3d hitPos = context.getHitPos();

        // this code sucks
        BlockPos flooredPos = new BlockPos(
                hitPos.getX() >= 0 ? (int) Math.floor(hitPos.getX()) : (int) ((Math.ceil(-hitPos.getX()) * -1)),
                hitPos.getY() >= 0 ? (int) Math.floor(hitPos.getY()) : ((int) (Math.ceil(-hitPos.getY()) * -1)),
                hitPos.getZ() >= 0 ? (int) Math.floor(hitPos.getZ()) : ((int) Math.ceil(-hitPos.getZ()) * -1)
        );

        BlockState state = world.getBlockState(flooredPos);

        // im not sorry
        Predicate<BlockPos> canReplace = blockPos -> world.getBlockState(blockPos).canReplace(context);
        if (state.isOf(ChipsBlocks.CHIPS_BLOCK)) {
            ActionResult result = tryPlaceAt(world, flooredPos, hitPos, blockType, context.getPlayer(), stack, canReplace);
            if (result == ActionResult.SUCCESS) {
                return ActionResult.SUCCESS;
            }
        }

        return tryPlaceAt(world, context.getBlockPos(), hitPos, blockType, context.getPlayer(), stack, canReplace);
    }

    private ActionResult tryPlaceAt(
            World world,
            BlockPos pos,
            Vec3d absHitPos,
            Block block,
            PlayerEntity player,
            ItemStack stack,
            Predicate<BlockPos> canReplace
    ) {
        BlockState state = world.getBlockState(pos);
        if (!state.isOf(ChipsBlocks.CHIPS_BLOCK)) {
            if (!canReplace.test(pos)) {
                return ActionResult.FAIL;
            }

            world.setBlockState(pos, ChipsBlocks.CHIPS_BLOCK.getDefaultState());
            state = world.getBlockState(pos);
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof ChipsBlockEntity chipsBlockEntity)) {
            return ActionResult.FAIL;
        }

        int targetCorner = getTargetCorner(absHitPos.subtract(Vec3d.of(pos)));
        if (chipsBlockEntity.hasCorner(targetCorner)) {
            absHitPos = absHitPos.subtract(new Vec3d(EPSILON, EPSILON, EPSILON));
            targetCorner = getTargetCorner(absHitPos.subtract(Vec3d.of(pos)));

            if (chipsBlockEntity.hasCorner(targetCorner)) {
                return ActionResult.FAIL;
            }
        }

        chipsBlockEntity.addChips(block, targetCorner);

        playPlaceSound(world, player, block, pos);
        world.emitGameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Emitter.of(player, state));
        stack.decrementUnlessCreative(1, player);

        return ActionResult.SUCCESS;
    }

    private void playPlaceSound(World world, PlayerEntity playerEntity, Block blockType, BlockPos blockPos) {
        BlockState state = blockType.getDefaultState();
        BlockSoundGroup blockSoundGroup = state.getSoundGroup();
        world.playSound(
                playerEntity,
                blockPos,
                this.getPlaceSound(state),
                SoundCategory.BLOCKS,
                (blockSoundGroup.getVolume() + 1.0F) / 2.0F,
                blockSoundGroup.getPitch() * 0.8F
        );
    }

    public int getTargetCorner(Vec3d relHitPos) {
        int currentCorner = 255;

        if (relHitPos.getX() >= 0.5f) {
            currentCorner &= ~1;
            currentCorner &= ~(1 << 2);

            currentCorner &= ~(1 << 4);
            currentCorner &= ~(1 << 6);
        } else {
            currentCorner &= ~(1 << 1);
            currentCorner &= ~(1 << 3);

            currentCorner &= ~(1 << 5);
            currentCorner &= ~(1 << 7);
        }

        if (relHitPos.getZ() >= 0.5f) {
            currentCorner &= ~1;
            currentCorner &= ~2;

            currentCorner &= ~(1 << 4);
            currentCorner &= ~(1 << 5);
        } else {
            currentCorner &= ~4;
            currentCorner &= ~8;

            currentCorner &= ~(1 << 6);
            currentCorner &= ~(1 << 7);
        }

        if (relHitPos.getY() >= 0.5f) {
            currentCorner &= ~(1 | 2 | 4 | 8);
        } else {
            currentCorner &= ~(16 | 32 | 64 | 128);
        }

        return currentCorner;
    }

    @Override
    public Text getName(ItemStack stack) {
        if (stack.contains(ChipsComponents.BLOCK_COMPONENT_COMPONENT)) {
            ChipsBlockItemComponent blockComponent = stack.get(ChipsComponents.BLOCK_COMPONENT_COMPONENT);
            Text blockName = blockComponent.block().getName();
            return blockName.copy().append(" ").append(Text.translatable("item.chips.chips_block.chip"));
        }
        return super.getName(stack);
    }
}
