package io.github.hawah.shakenstir.content.dataComponent;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Objects;
import java.util.function.Supplier;

public final class FluidStackDataComponent {
    public static final Codec<FluidStackDataComponent> CODEC = FluidStack.CODEC.xmap(FluidStackDataComponent::new, FluidStackDataComponent::fluidStack);
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackDataComponent> STREAM_CODEC = StreamCodec.composite(
            FluidStack.STREAM_CODEC,
            FluidStackDataComponent::fluidStack,
            FluidStackDataComponent::new
    );
    public static final FluidStackDataComponent EMPTY = new FluidStackDataComponent(FluidStack.EMPTY);
    private final Supplier<FluidStack> fluidStack;

    public FluidStackDataComponent(FluidStack fluidStack) {
        this.fluidStack = () -> fluidStack;
    }

    public FluidStackDataComponent(Identifier fluid, int amount) {
        this.fluidStack = () -> new FluidStack(BuiltInRegistries.FLUID.getValue(fluid), amount);
    }

    public boolean isEmpty() {
        return fluidStack.get().isEmpty();
    }

    public FluidStack fluidStack() {
        return fluidStack.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FluidStackDataComponent) obj;
        return Objects.equals(this.fluidStack, that.fluidStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluidStack);
    }

    @Override
    public String toString() {
        return "FluidStackDataComponent[" +
                "fluidStack=" + fluidStack + ']';
    }

}
