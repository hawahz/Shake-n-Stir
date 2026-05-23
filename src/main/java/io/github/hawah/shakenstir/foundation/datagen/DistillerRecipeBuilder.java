package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.content.recipe.DistillerRecipe;
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

public class DistillerRecipeBuilder implements RecipeBuilder {
    protected final ItemStackTemplate result;
    private final List<Ingredient> inputItems;
    private final FluidIngredient inputFluid;
    protected String group = "";
    protected boolean showNotification = true;

    protected final RecipeUnlockAdvancementBuilder advancementBuilder;
    protected final int cookingTime;

    public DistillerRecipeBuilder(ItemStackTemplate result, List<Ingredient> inputItems, FluidIngredient inputFluid, int cookingTime, RecipeCategory category) {
        this.result = result;
        this.inputItems = inputItems;
        this.inputFluid = inputFluid;
        this.advancementBuilder = new RecipeUnlockAdvancementBuilder();
        this.category = category;
        this.cookingTime = cookingTime;
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
        DistillerRecipe recipe = new DistillerRecipe(
                RecipeBuilder.createCraftingCommonInfo(this.showNotification),
                this.inputItems,
                this.inputFluid,
                this.cookingTime,
                this.result
        );
        output.accept(
                location, recipe, this.advancementBuilder.build(output, location, this.category)
        );
    }
}
