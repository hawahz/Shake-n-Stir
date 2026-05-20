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

    public static final EffectData BRANDY_EFFECT = EffectData.spirit(
            MobEffects.JUMP_BOOST, MobEffects.SLOWNESS
    );
    public static final EffectData GIN_EFFECT = EffectData.spirit(
            MobEffects.LUCK, MobEffects.UNLUCK
    );
    public static final EffectData RUM_EFFECT = EffectData.spirit(
            MobEffects.DOLPHINS_GRACE, MobEffects.WEAKNESS
    );
    public static final EffectData VODKA_EFFECT = EffectData.spirit(
            MobEffects.JUMP_BOOST, MobEffects.WEAKNESS
    );
    public static final EffectData WHISKEY_EFFECT = EffectData.spirit(
            MobEffects.FIRE_RESISTANCE, MobEffects.WEAKNESS
    );
    public static final EffectData TEQUILA_EFFECT = EffectData.spirit(
            MobEffects.HASTE, MobEffects.MINING_FATIGUE
    );

    public static final DeferredHolder<SpiritData, SpiritData> BRANDY = register("brandy", FluidRegistries.BRANDY_SOURCE_FLUID_BLOCK, BRANDY_EFFECT);
    public static final DeferredHolder<SpiritData, SpiritData> GIN = register("gin", FluidRegistries.GIN_SOURCE_FLUID_BLOCK, GIN_EFFECT);
    public static final DeferredHolder<SpiritData, SpiritData> RUM = register("rum", FluidRegistries.RUM_SOURCE_FLUID_BLOCK, RUM_EFFECT);
    public static final DeferredHolder<SpiritData, SpiritData> VODKA = register("vodka", FluidRegistries.VODKA_SOURCE_FLUID_BLOCK, VODKA_EFFECT);
    public static final DeferredHolder<SpiritData, SpiritData> WHISKEY = register("whiskey", FluidRegistries.WHISKY_SOURCE_FLUID_BLOCK, WHISKEY_EFFECT);
    public static final DeferredHolder<SpiritData, SpiritData> TEQUILA = register("tequila", FluidRegistries.TEQUILA_SOURCE_FLUID_BLOCK, TEQUILA_EFFECT);
    public static final DeferredHolder<SpiritData, SpiritData> FALLBACK = register("scotch", Fluids.EMPTY.builtInRegistryHolder(), EffectData.of(MobEffects.STRENGTH, List.of()));


    public static DeferredHolder<SpiritData, SpiritData> register(String registryKey, Holder<Fluid> fluidType, EffectData effectData) {
        return COCKTAIL_TYPE.register(registryKey, () -> new SpiritData(fluidType, effectData));
    }

    public static DeferredHolder<SpiritData, SpiritData> fallback() {
        return FALLBACK;
    }

}
