package io.github.hawah.shakenstir.content.recipe;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record DistillerRecipeInput(List<ItemStack> items, FluidStack fluidStack) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        if (index >= items.size()) {
            return ItemStack.EMPTY;
        }
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }
}
