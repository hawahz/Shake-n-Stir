package io.github.hawah.shakenstir.content.recipe.shake;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public record ShakeRecipeInput(List<ItemStack> items, List<FluidStack> fluidStacks, int shakeTime) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        if (index > items.size()) {
            return null;
        }
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        boolean ret = RecipeInput.super.isEmpty() && (fluidStacks.isEmpty() || fluidStacks.stream().anyMatch(FluidStack::isEmpty));
        return ret;
    }
}
