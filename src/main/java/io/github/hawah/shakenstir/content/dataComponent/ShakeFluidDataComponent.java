package io.github.hawah.shakenstir.content.dataComponent;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

@Deprecated
public record ShakeFluidDataComponent(List<FluidStack> fluidStacks) implements IFluidDataHolder{

    public static final ShakeFluidDataComponent EMPTY = new ShakeFluidDataComponent(NonNullList.of(FluidStack.EMPTY));

    public static final Codec<ShakeFluidDataComponent> CODEC = FluidStack.CODEC.listOf().xmap(ShakeFluidDataComponent::new, ShakeFluidDataComponent::fluidStacks);
    public static final StreamCodec<RegistryFriendlyByteBuf, ShakeFluidDataComponent> STREAM_CODEC = StreamCodec.composite(
            FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()), ShakeFluidDataComponent::fluidStacks,
            ShakeFluidDataComponent::new
    );

    public int fluidVolume() {
        return fluidStacks.size();
    }

    @Override
    public int fluidVariety() {
        return 0;
    }


}
