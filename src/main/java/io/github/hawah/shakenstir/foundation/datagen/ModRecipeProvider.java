package io.github.hawah.shakenstir.foundation.datagen;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.recipe.ingredient.FluidIngredient;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModRecipeProvider extends RecipeProvider {
    private final HolderLookup.RegistryLookup<Item> items;
    protected ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
        items = registries.lookupOrThrow(Registries.ITEM);
    }

    @Override
    protected void buildRecipes() {

        new ShakeRecipeBuilder(
                new ItemStackTemplate(ItemRegistries.CONTENT_HOLDER, 1, DataComponentPatch.builder().set(DataComponentTypeRegistries.SHAKE_BUBBLES, true).build()),
                List.of(
                        new FluidIngredient(FluidRegistries.GIN_SOURCE_FLUID_BLOCK.getId(), 1000)
                ),
                List.of(
                        Ingredient.of(items.getOrThrow(Tags.Items.FOODS_FRUIT))
                ),
                20,
                RecipeCategory.FOOD
        )
                .unlockedBy("has_shake", this.has(ItemRegistries.SHAKE))
                .save(output);
    }

    public static class Runner extends RecipeProvider.Runner {
        // Get the parameters from the `GatherDataEvent`s.
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new ModRecipeProvider(provider, output);
        }

        @Override
        public String getName() {
            return "shake_recipe";
        }
    }

    class Builder {
        private Supplier<Item> result;
        private int resultAmount = 1;
        private List<Ingredient> ingredients;
        private List<FluidIngredient> fluidIngredients;
        private int shakeTime = 20;

        public Builder result(Supplier<Item> result) {
            this.result = result;
            return this;
        }

        public Builder resultAmount(int amount) {
            this.resultAmount = amount;
            return this;
        }

        public Builder with(ItemLike... ingredient) {
            this.ingredients.add(Ingredient.of(ingredient));
            return this;
        }

        public Builder with(TagKey<Item> tag) {
            this.ingredients.add(Ingredient.of(items.getOrThrow(tag)));
            return this;
        }

        public Builder with(FluidIngredient fluidIngredient) {
            this.fluidIngredients.add(fluidIngredient);
            return this;
        }

        public Builder with(DeferredHolder<Fluid, FlowingFluid> fluid, int amount) {
            this.fluidIngredients.add(new FluidIngredient(fluid.getId(), amount));
            return this;
        }

        public Builder shake(int shakeTime) {
            this.shakeTime = shakeTime;
            return this;
        }

        public ShakeRecipeBuilder build() {
            return new ShakeRecipeBuilder(
                    new ItemStackTemplate(result.get(), resultAmount),
                    fluidIngredients,
                    ingredients,
                    shakeTime,
                    RecipeCategory.FOOD
            );
        }
    }
}
