package io.github.hawah.shakenstir.foundation.recipe.datapack.spirit;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import io.github.hawah.shakenstir.foundation.recipe.datapack.EffectData;
import io.github.hawah.shakenstir.foundation.datapack.Registries;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.material.Fluid;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class Spirits {
    public static final EffectData BRANDY_EFFECT = EffectData.spirit(
            MobEffects.SPEED, MobEffects.SLOWNESS
    );
    public static final EffectData GIN_EFFECT = EffectData.spirit(
            MobEffects.HEALTH_BOOST, MobEffects.WITHER
    );
    public static final EffectData RUM_EFFECT = EffectData.spirit(
            MobEffects.STRENGTH, MobEffects.WEAKNESS
    );
    public static final EffectData VODKA_EFFECT = EffectData.spirit(
            MobEffects.JUMP_BOOST, MobEffects.WEAKNESS
    );
    public static final EffectData WHISKEY_EFFECT = EffectData.spirit(
            MobEffects.FIRE_RESISTANCE, MobEffects.POISON
    );
    public static final EffectData TEQUILA_EFFECT = EffectData.spirit(
            MobEffects.HASTE, MobEffects.MINING_FATIGUE
    );

    public static final ResourceKey<FluidData> BRANDY = fluidKey("brandy");
    public static final ResourceKey<FluidData> GIN = fluidKey("gin");
    public static final ResourceKey<FluidData> RUM = fluidKey("rum");
    public static final ResourceKey<FluidData> VODKA = fluidKey("vodka");
    public static final ResourceKey<FluidData> WHISKEY = fluidKey("whiskey");
    public static final ResourceKey<FluidData> TEQUILA = fluidKey("tequila");

    private static final Map<ResourceKey<FluidData>, FluidData> ENTRIES = new LinkedHashMap<>();

    static {
        ENTRIES.put(BRANDY, new FluidData(FluidRegistries.BRANDY_SOURCE, BRANDY_EFFECT, 250 /*MAGIC*/));
        ENTRIES.put(GIN, new FluidData(FluidRegistries.GIN_SOURCE, GIN_EFFECT, 250 /*MAGIC*/));
        ENTRIES.put(RUM, new FluidData(FluidRegistries.RUM_SOURCE, RUM_EFFECT, 250 /*MAGIC*/));
        ENTRIES.put(VODKA, new FluidData(FluidRegistries.VODKA_SOURCE, VODKA_EFFECT, 250 /*MAGIC*/));
        ENTRIES.put(WHISKEY, new FluidData(FluidRegistries.WHISKY_SOURCE, WHISKEY_EFFECT, 250 /*MAGIC*/));
        ENTRIES.put(TEQUILA, new FluidData(FluidRegistries.TEQUILA_SOURCE, TEQUILA_EFFECT, 250 /*MAGIC*/));
    }

    public static ResourceKey<FluidData> fluidKey(String name) {
        return ResourceKey.create(Registries.FLUID_REGISTRY_KEY, ShakenStir.asResource(name));
    }

    public static void forEachEntry(BiConsumer<ResourceKey<FluidData>, FluidData> consumer) {
        ENTRIES.forEach(consumer);
    }

    public static Optional<FluidData> getBuiltIn(Holder<Fluid> fluidType) {
        return ENTRIES.values().stream()
                .filter(data -> data.fluidType().equals(fluidType))
                .findFirst();
    }
}