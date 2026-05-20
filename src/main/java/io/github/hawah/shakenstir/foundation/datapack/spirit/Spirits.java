package io.github.hawah.shakenstir.foundation.datapack.spirit;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import io.github.hawah.shakenstir.foundation.datapack.EffectData;
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

    public static final ResourceKey<SpiritData> BRANDY = spiritKey("brandy");
    public static final ResourceKey<SpiritData> GIN = spiritKey("gin");
    public static final ResourceKey<SpiritData> RUM = spiritKey("rum");
    public static final ResourceKey<SpiritData> VODKA = spiritKey("vodka");
    public static final ResourceKey<SpiritData> WHISKEY = spiritKey("whiskey");
    public static final ResourceKey<SpiritData> TEQUILA = spiritKey("tequila");

    private static final Map<ResourceKey<SpiritData>, SpiritData> ENTRIES = new LinkedHashMap<>();

    static {
        ENTRIES.put(BRANDY, new SpiritData(FluidRegistries.BRANDY_SOURCE_FLUID_BLOCK, BRANDY_EFFECT));
        ENTRIES.put(GIN, new SpiritData(FluidRegistries.GIN_SOURCE_FLUID_BLOCK, GIN_EFFECT));
        ENTRIES.put(RUM, new SpiritData(FluidRegistries.RUM_SOURCE_FLUID_BLOCK, RUM_EFFECT));
        ENTRIES.put(VODKA, new SpiritData(FluidRegistries.VODKA_SOURCE_FLUID_BLOCK, VODKA_EFFECT));
        ENTRIES.put(WHISKEY, new SpiritData(FluidRegistries.WHISKY_SOURCE_FLUID_BLOCK, WHISKEY_EFFECT));
        ENTRIES.put(TEQUILA, new SpiritData(FluidRegistries.TEQUILA_SOURCE_FLUID_BLOCK, TEQUILA_EFFECT));
    }

    public static ResourceKey<SpiritData> spiritKey(String name) {
        return ResourceKey.create(Registries.SPIRIT_REGISTRY_KEY, ShakenStir.asResource(name));
    }

    public static void forEachEntry(BiConsumer<ResourceKey<SpiritData>, SpiritData> consumer) {
        ENTRIES.forEach(consumer);
    }

    public static Optional<SpiritData> getBuiltIn(Holder<Fluid> fluidType) {
        return ENTRIES.values().stream()
                .filter(data -> data.fluidType().equals(fluidType))
                .findFirst();
    }
}