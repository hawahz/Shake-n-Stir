package io.github.hawah.shakenstir.foundation.recipe.datapack.cocktaileType;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import io.github.hawah.shakenstir.foundation.recipe.datapack.EffectData;
import io.github.hawah.shakenstir.foundation.recipe.datapack.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class CocktailTypes {
    public static final CocktailType SOUR_VALUE = new CocktailType(ShakenStir.asResource("sour"), List.of(
            EffectData.cocktail(MobEffectRegistries.LEMON, MobEffects.WEAKNESS)
    ));
    public static final CocktailType FIZZ_VALUE = new CocktailType(ShakenStir.asResource("fizz"), List.of(
            EffectData.cocktail(MobEffects.SPEED, MobEffects.SLOWNESS)
    ));
    public static final CocktailType COCKTAIL_VALUE = new CocktailType(ShakenStir.asResource("cocktail"), List.of(

    ));
    public static final CocktailType HIGHBALL_VALUE = new CocktailType(ShakenStir.asResource("highball"), List.of(

    ));
    public static final CocktailType TONIC_VALUE = new CocktailType(ShakenStir.asResource("tonic"), List.of(

    ));
    public static final CocktailType COLADA_VALUE = new CocktailType(ShakenStir.asResource("colada"), List.of(

    ));
    public static final CocktailType SUSPICIOUS_VALUE = new CocktailType(ShakenStir.asResource("suspicious"), List.of(
            EffectData.suspicious()
    ));

    public static final ResourceKey<CocktailType> SOUR = cocktailKey("sour");
    public static final ResourceKey<CocktailType> FIZZ = cocktailKey("fizz");
    public static final ResourceKey<CocktailType> COCKTAIL = cocktailKey("cocktail");
    public static final ResourceKey<CocktailType> HIGHBALL = cocktailKey("highball");
    public static final ResourceKey<CocktailType> TONIC = cocktailKey("tonic");
    public static final ResourceKey<CocktailType> COLADA = cocktailKey("colada");
    public static final ResourceKey<CocktailType> SUSPICIOUS = cocktailKey("suspicious");

    private static final Map<ResourceKey<CocktailType>, CocktailType> ENTRIES = new LinkedHashMap<>();

    static {
        ENTRIES.put(SOUR, SOUR_VALUE);
        ENTRIES.put(FIZZ, FIZZ_VALUE);
        ENTRIES.put(COCKTAIL, COCKTAIL_VALUE);
        ENTRIES.put(HIGHBALL, HIGHBALL_VALUE);
        ENTRIES.put(TONIC, TONIC_VALUE);
        ENTRIES.put(COLADA, COLADA_VALUE);
        ENTRIES.put(SUSPICIOUS, SUSPICIOUS_VALUE);
    }

    public static ResourceKey<CocktailType> cocktailKey(String name) {
        return ResourceKey.create(Registries.COCKTAIL_REGISTRY_KEY, ShakenStir.asResource(name));
    }

    public static void forEachEntry(BiConsumer<ResourceKey<CocktailType>, CocktailType> consumer) {
        ENTRIES.forEach(consumer);
    }

    public static CocktailType getBuiltIn(ResourceKey<CocktailType> key) {
        CocktailType value = ENTRIES.get(key);
        return value != null ? value : CocktailType.EMPTY;
    }
}