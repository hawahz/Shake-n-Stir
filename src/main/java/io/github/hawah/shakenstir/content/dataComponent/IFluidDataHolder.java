package io.github.hawah.shakenstir.content.dataComponent;

import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public interface IFluidDataHolder {
    List<FluidStack> fluidStacks();
    int fluidVolume();
}
