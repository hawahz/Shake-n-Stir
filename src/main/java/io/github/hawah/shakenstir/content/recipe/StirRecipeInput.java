package io.github.hawah.shakenstir.content.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public record StirRecipeInput(List<ItemStack> items, List<FluidStack> fluidStacks) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        if (index > items.size()) {
            return null;
        }
        return items.get(index);
    }

    @Override
    public int size() {
        return 0;
    }
}
