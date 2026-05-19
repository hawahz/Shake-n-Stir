package io.github.hawah.shakenstir.foundation.datapack.cocktaileType;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.foundation.datapack.DatapackRegistries;
import io.github.hawah.shakenstir.foundation.datapack.EffectData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class CocktailTypes {
    public static final DeferredRegister<CocktailType> COCKTAIL_TYPE = DeferredRegister.create(DatapackRegistries.COCKTAIL_REGISTRY_KEY, ShakenStir.MODID);

    public static final DeferredHolder<CocktailType, CocktailType> SOUR = register("sour");
    public static final DeferredHolder<CocktailType, CocktailType> FIZZ = register("fizz");
    public static final DeferredHolder<CocktailType, CocktailType> COCKTAIL = register("cocktail");
    public static final DeferredHolder<CocktailType, CocktailType> HIGHBALL = register("highball");
    public static final DeferredHolder<CocktailType, CocktailType> TONIC = register("tonic");
    public static final DeferredHolder<CocktailType, CocktailType> COLADA = register("colada");


    private static @NonNull DeferredHolder<CocktailType, CocktailType> register(String registryKey, EffectData... effects) {
        return COCKTAIL_TYPE.register(registryKey, () -> new CocktailType(ShakenStir.asResource(registryKey), ShakenStir.asResource(registryKey), List.of(effects)));
    }

    public static void register(IEventBus modEventBus) {
        COCKTAIL_TYPE.register(modEventBus);
    }
}
