package io.github.hawah.shakenstir.compat.jei;

import io.github.hawah.shakenstir.foundation.events.ShakingEvents;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;

import java.util.List;

public class SnsJeiRecipes {
    public static <I extends RecipeInput, T extends Recipe<I>> void registerRecipe(IRecipeRegistration registration, IRecipeType<T> type, Class<T> clazz) {
        RecipeMap recipeMap = ShakingEvents.recipeMap;
        if (recipeMap == null) {
            return;
        }

        List<T> shakeRecipes = recipeMap.values().stream()
                .map(RecipeHolder::value)
                .filter(clazz::isInstance)
                .map(r -> (T) r)
                .toList();

        registration.addRecipes(type, shakeRecipes);
    }
}
