package io.github.hawah.shakenstir.content.dataComponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;

public record DeferredFluidStackHolder(Identifier fluid, int amount) {

    public static final Codec<DeferredFluidStackHolder> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("fluid").forGetter(DeferredFluidStackHolder::fluid),
            Codec.INT.fieldOf("amount").forGetter(DeferredFluidStackHolder::amount)
    ).apply(inst, DeferredFluidStackHolder::new));

    public static final StreamCodec<ByteBuf, DeferredFluidStackHolder> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, DeferredFluidStackHolder::fluid,
            ByteBufCodecs.INT, DeferredFluidStackHolder::amount,
            DeferredFluidStackHolder::new
    );

    public DeferredFluidStackHolder(DeferredHolder<Fluid, FlowingFluid> fluid, int amount) {
        this(fluid.getId(), amount);
    }

    public FluidStack toFluidStack() {
        return new FluidStack(BuiltInRegistries.FLUID.getValue(fluid), amount);
    }
}
