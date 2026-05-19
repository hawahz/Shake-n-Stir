package io.github.hawah.shakenstir.foundation.datapack.spirit;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import io.github.hawah.shakenstir.foundation.datapack.DatapackRegistries;
import io.github.hawah.shakenstir.foundation.datapack.EffectData;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class Spirits {
    public static final DeferredRegister<SpiritData> COCKTAIL_TYPE = DeferredRegister.create(DatapackRegistries.SPIRIT_REGISTRY_KEY, ShakenStir.MODID);

    public static final DeferredHolder<SpiritData, SpiritData> BRANDY = register("brandy", FluidRegistries.BRANDY_SOURCE_FLUID_BLOCK, EffectData.of(MobEffects.JUMP_BOOST, List.of(1, 2, 3, 4)));
    public static final DeferredHolder<SpiritData, SpiritData> GIN = register("gin", FluidRegistries.GIN_SOURCE_FLUID_BLOCK, EffectData.of(MobEffects.LUCK, List.of(1, 2, 3, 4)));
    public static final DeferredHolder<SpiritData, SpiritData> RUM = register("rum", FluidRegistries.RUM_SOURCE_FLUID_BLOCK, EffectData.of(MobEffects.DOLPHINS_GRACE, List.of(1, 2, 3, 4)));
    public static final DeferredHolder<SpiritData, SpiritData> VODKA = register("vodka", FluidRegistries.VODKA_SOURCE_FLUID_BLOCK, EffectData.of(MobEffects.STRENGTH, List.of(1, 2, 3, 4)));
    public static final DeferredHolder<SpiritData, SpiritData> WHISKEY = register("whiskey", FluidRegistries.WHISKY_SOURCE_FLUID_BLOCK, EffectData.of(MobEffects.FIRE_RESISTANCE, List.of(1, 2, 3, 4)));
    public static final DeferredHolder<SpiritData, SpiritData> TEQUILA = register("tequila", FluidRegistries.TEQUILA_SOURCE_FLUID_BLOCK, EffectData.of(MobEffects.HASTE, List.of(1, 2, 3, 4)));
    public static final DeferredHolder<SpiritData, SpiritData> FALLBACK = register("scotch", Fluids.EMPTY.builtInRegistryHolder(), EffectData.of(MobEffects.STRENGTH, List.of()));

    public static DeferredHolder<SpiritData, SpiritData> register(String registryKey, Holder<Fluid> fluidType, EffectData effectData) {
        return COCKTAIL_TYPE.register(registryKey, () -> new SpiritData(fluidType, effectData));
    }

    public static DeferredHolder<SpiritData, SpiritData> fallback() {
        return FALLBACK;
    }

}
