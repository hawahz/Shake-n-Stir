package io.github.hawah.shakenstir.compat.jei;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.compat.jei.category.ShakeCategory;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.events.ShakingEvents;
import io.github.hawah.shakenstir.foundation.recipe.shake.ShakeRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;

import java.util.List;

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
        RecipeMap recipeMap = ShakingEvents.recipeMap;
        if (recipeMap == null) {
            return;
        }

        // 用 values() 遍历全部配方 + instanceof 过滤
        // 不再用 byType(), 因为 RecipeType.simple() 的匿名类没有覆写 equals(),
        // 导致 RecipeMap 内部用 == 比较, DeferredRegister 返回的实例匹配不上
        List<ShakeRecipe> shakeRecipes = recipeMap.values().stream()
                .map(RecipeHolder::value)
                .filter(r -> r instanceof ShakeRecipe)
                .map(r -> (ShakeRecipe) r)
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
