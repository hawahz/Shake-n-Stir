package io.github.hawah.shakenstir.compat.jei;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.compat.jei.category.DistillerCategory;
import io.github.hawah.shakenstir.compat.jei.category.ShakeCategory;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.recipe.DistillerRecipe;
import io.github.hawah.shakenstir.foundation.recipe.shake.ShakeRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;

@JeiPlugin
public class JeiSnSPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return ShakenStir.asResource("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new ShakeCategory(),
                new DistillerCategory()
        );
        // 注册自定义配方类别

    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        SnsJeiRecipes.registerRecipe(registration, ShakeCategory.SHAKE_TYPE, ShakeRecipe.class);
        SnsJeiRecipes.registerRecipe(registration, DistillerCategory.TYPE, DistillerRecipe.class);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        // 注册GUI交互器
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // 注册配方催化剂
        registration.addCraftingStation(ShakeCategory.SHAKE_TYPE, ItemRegistries.SHAKER.toStack());
        registration.addCraftingStation(DistillerCategory.TYPE, ItemRegistries.DISTILLER.toStack());
    }
}
