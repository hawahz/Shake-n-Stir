package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    protected ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {

        new ShakeRecipeBuilder(
                new ItemStackTemplate(ItemRegistries.GIN),
                List.of(
                        new FluidStack(FluidRegistries.GIN_SOURCE_FLUID_BLOCK.get(), 1000)
                ),
                List.of(
                        Ingredient.of(ItemRegistries.SHAKE)
                ),
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
}
