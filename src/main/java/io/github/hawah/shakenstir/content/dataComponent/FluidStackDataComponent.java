package io.github.hawah.shakenstir.content.dataComponent;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;

public record FluidStackDataComponent(FluidStack fluidStack) {
    public static final Codec<FluidStackDataComponent> CODEC = FluidStack.CODEC.xmap(FluidStackDataComponent::new, FluidStackDataComponent::fluidStack);
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackDataComponent> STREAM_CODEC = StreamCodec.composite(
            FluidStack.STREAM_CODEC,
            FluidStackDataComponent::fluidStack,
            FluidStackDataComponent::new
    );
    public static final FluidStackDataComponent EMPTY = new FluidStackDataComponent(FluidStack.EMPTY);

    public boolean isEmpty() {
        return fluidStack.isEmpty();
    }
}
