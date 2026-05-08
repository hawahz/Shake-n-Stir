package io.github.hawah.shakenstir.content.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public record ShakeRecipeInput(List<ItemStack> items, List<FluidStack> fluidStacks) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {

        return null;
    }

    @Override
    public int size() {
        return items.size();
    }
}
