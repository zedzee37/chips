package zedzee.github.io.chips.mixin;

import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(PreparedRecipes.class)
public interface RecipeMapAccessor {
    @Accessor("byKey")
    Map<RegistryKey<Recipe<?>>, RecipeEntry<?>> getRecipeMap();
}
