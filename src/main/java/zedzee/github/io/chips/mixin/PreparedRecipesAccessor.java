package zedzee.github.io.chips.mixin;

import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.ServerRecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerRecipeManager.class)
public interface PreparedRecipesAccessor {
    @Accessor("preparedRecipes")
    PreparedRecipes getPreparedRecipes();
}
