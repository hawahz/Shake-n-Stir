package io.github.hawah.shakenstir.compat.jei;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.compat.jei.category.ShakeCategory;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.recipe.shake.ShakeRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;
import java.util.Optional;

@JeiPlugin
public class JeiSnSPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return ShakenStir.asResource("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new ShakeCategory()
        );
        // 注册自定义配方类别

    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().getSingleplayerServer().getRecipeManager();
        // 注册具体的配方实例

        List<ShakeRecipe> shakeRecipes = recipeManager.getRecipes().stream()
                .map(RecipeHolder::value)
                .filter(recipeHolder -> recipeHolder instanceof ShakeRecipe)
                .map(recipe -> (Optional<ShakeRecipe>) (recipe instanceof ShakeRecipe shakeRecipe? Optional.of(shakeRecipe): Optional.empty()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        registration.addRecipes(ShakeCategory.SHAKE_TYPE, shakeRecipes);

    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {

        // 注册GUI交互器

    }



    // 可选项，但建议加上这一项，可以增强你mod的引导性
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {

        // 注册配方催化剂
        registration.addCraftingStation(ShakeCategory.SHAKE_TYPE, ItemRegistries.SHAKER.toStack());
    }

    private static IIngredientManager INGREDIENT_MANAGER;
    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        INGREDIENT_MANAGER = jeiRuntime.getIngredientManager();
    }

    // Getter
    public static IIngredientManager ingredientManager() {
        return INGREDIENT_MANAGER;
    }
}
