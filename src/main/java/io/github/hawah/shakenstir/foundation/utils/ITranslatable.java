package io.github.hawah.shakenstir.foundation.utils;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public interface ITranslatable {
    MutableComponent translate(List<FluidStack> fluidStacks, List<ItemStack> itemStacks);
}
