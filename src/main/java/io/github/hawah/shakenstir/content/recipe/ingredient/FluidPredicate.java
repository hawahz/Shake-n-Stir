package io.github.hawah.shakenstir.content.recipe.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.function.Predicate;

public class FluidPredicate implements Predicate<FluidStack>, StackedContents.IngredientInfo<Holder<Fluid>> {

    public static final Codec<Holder<Fluid>> FLUID_CODEC = BuiltInRegistries.FLUID
            .holderByNameCodec()
            .validate(DataResult::success);
    public static final Codec<HolderSet<Fluid>> NON_AIR_HOLDER_SET_CODEC = HolderSetCodec.create(Registries.FLUID, FLUID_CODEC, false);
    public static final Codec<FluidPredicate> CODEC = ExtraCodecs.nonEmptyHolderSet(NON_AIR_HOLDER_SET_CODEC).xmap(FluidPredicate::new, i -> i.values);

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidPredicate> CONTENTS_STREAM_CODEC = ByteBufCodecs.holderSet(Registries.FLUID)
            .map(FluidPredicate::new, i -> i.values);
    private final HolderSet<Fluid> values;

    public FluidPredicate(HolderSet<Fluid> values) {
        if (values.isImmediatelyResolvable())//Neo: Skip validating holderset contents for holdersets if they are not immediately resolvable
            values.unwrap().ifRight(directValues -> {
                if (directValues.isEmpty()) {
                    throw new UnsupportedOperationException("Ingredients can't be empty");
                } else if (directValues.contains(Items.AIR.builtInRegistryHolder())) {
                    throw new UnsupportedOperationException("Ingredient can't contain air");
                }
            });
        this.values = values;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return fluidStack.is(this.values);
    }

    @Override
    public boolean acceptsItem(Holder<Fluid> item) {
        return this.values.contains(item);
    }
}
