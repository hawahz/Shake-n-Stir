package io.github.hawah.shakenstir.content.recipe.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public record FluidIngredient(FluidPredicate fluidId, int amount) {

    public static final Codec<FluidIngredient> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            FluidPredicate.CODEC.fieldOf("fluidId").forGetter(FluidIngredient::fluidId),
            Codec.INT.fieldOf("amount").forGetter(FluidIngredient::amount)
    ).apply(inst, FluidIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> STREAM_CODEC = StreamCodec.composite(
            FluidPredicate.CONTENTS_STREAM_CODEC, FluidIngredient::fluidId,
            ByteBufCodecs.INT, FluidIngredient::amount,
            FluidIngredient::new
    );

    public FluidIngredient(HolderSet<Fluid> fluidHolderSet, int amount) {
        this(new FluidPredicate(fluidHolderSet), amount);
    }

    public static FluidIngredient of(HolderSet<Fluid> fluidHolderSet, int amount) {
        return new FluidIngredient(fluidHolderSet, amount);
    }
//    public static FluidIngredient of(Identifier fluidId, int amount) {
//        return new FluidIngredient(HolderSet.direct(itemLike.asItem().builtInRegistryHolder()), amount);
//    }
//
//    public static FluidIngredient of(Identifier fluidId) {
//        return new FluidIngredient(List.of(fluidId), 1000);
//    }

    public static FluidIngredient of(DeferredHolder<Fluid, FlowingFluid> fluidId, int amount) {
        return new FluidIngredient(new FluidPredicate(HolderSet.direct(fluidId.get().builtInRegistryHolder())), amount);
    }

//    public static FluidIngredient of(Stream<Holder<Fluid>> stream, int amount) {
//        ;
//        return new FluidIngredient(stream.map(holder -> holder.getKey().identifier()).toList(), amount);
//    }

    public boolean match(FluidStack fluidStack) {
        return this.fluidId.test(fluidStack) && fluidStack.getAmount() >= this.amount;
    }
}
