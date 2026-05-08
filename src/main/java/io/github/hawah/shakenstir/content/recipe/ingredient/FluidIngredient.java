package io.github.hawah.shakenstir.content.recipe.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;

public record FluidIngredient(Identifier fluidId, int amount) {

    public static final Codec<FluidIngredient> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("fluidId").forGetter(FluidIngredient::fluidId),
            Codec.INT.fieldOf("amount").forGetter(FluidIngredient::amount)
    ).apply(inst, FluidIngredient::new));

    public static final StreamCodec<ByteBuf, FluidIngredient> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, FluidIngredient::fluidId,
            ByteBufCodecs.INT, FluidIngredient::amount,
            FluidIngredient::new
    );
    public static FluidIngredient of(Identifier fluidId, int amount) {
        return new FluidIngredient(fluidId, amount);
    }

    public static FluidIngredient of(Identifier fluidId) {
        return new FluidIngredient(fluidId, 1000);
    }

    public static FluidIngredient of(DeferredHolder<Fluid, FlowingFluid> fluidId, int amount) {
        return new FluidIngredient(fluidId.getId(), amount);
    }

    public FluidStack toFluidStack() {
        Fluid fluid = BuiltInRegistries.FLUID.getValue(this.fluidId);
        return new FluidStack(fluid, this.amount);
    }
}
