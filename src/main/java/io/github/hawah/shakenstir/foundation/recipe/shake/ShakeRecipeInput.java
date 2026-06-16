package io.github.hawah.shakenstir.foundation.recipe.shake;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ShakeRecipeInput(List<ItemStack> items, List<FluidStack> fluidStacks, int shakeTime) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        if (index > items.size()) {
            return ItemStack.EMPTY;
        }
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return RecipeInput.super.isEmpty() && (fluidStacks.isEmpty() || fluidStacks.stream().anyMatch(FluidStack::isEmpty));
    }
}
