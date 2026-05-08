package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.content.recipe.ShakeRecipe;
import io.github.hawah.shakenstir.content.recipe.ingredient.FluidIngredient;
import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ShakeRecipeBuilder implements RecipeBuilder {
    protected final ItemStackTemplate result;
    private final List<FluidIngredient> inputFluid;
    private final List<Ingredient> inputItem;
    protected String group = "";
    protected boolean showNotification = true;

    // 提供构建配方解锁进度的通用方法。
    // 如果使用此方法，构建器还必须指定一个 `RecipeCategory` 来确定输出文件夹。
    protected final RecipeUnlockAdvancementBuilder advancementBuilder;
    protected final int shakeTime;

    public ShakeRecipeBuilder(ItemStackTemplate result, List<FluidIngredient> inputFluid, List<Ingredient> inputItem, int shakeTime, RecipeCategory category) {
        this.result = result;
        this.inputFluid = inputFluid;
        this.inputItem = inputItem;
        this.advancementBuilder = new RecipeUnlockAdvancementBuilder();
        this.category = category;
        this.shakeTime = shakeTime;
    }

    protected final RecipeCategory category;
    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.advancementBuilder.unlockedBy(name, criterion);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String group) {
        this.group = Objects.requireNonNullElse(group, "");
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(this.result);
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> location) {
        ShakeRecipe recipe = new ShakeRecipe(
                RecipeBuilder.createCraftingCommonInfo(this.showNotification),
                this.inputFluid,
                this.inputItem,
                this.result,
                shakeTime
        );
        output.accept(
                location, recipe, this.advancementBuilder.build(output, location, this.category)
        );
    }
}
