package io.github.hawah.shakenstir.foundation.datapack.cocktaileType;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.foundation.datapack.DatapackRegistries;
import io.github.hawah.shakenstir.foundation.datapack.EffectData;
import io.github.hawah.shakenstir.foundation.datapack.RegistryHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Supplier;

public class CocktailTypes {
    public static final DeferredRegister<CocktailType> COCKTAIL_TYPE = DeferredRegister.create(DatapackRegistries.COCKTAIL_REGISTRY_KEY, ShakenStir.MODID);

    public static final RegistryHolder<CocktailType> SOUR = register("sour");
    public static final RegistryHolder<CocktailType> FIZZ = register("fizz");
    public static final RegistryHolder<CocktailType> COCKTAIL = register("cocktail");
    public static final RegistryHolder<CocktailType> HIGHBALL = register("highball");
    public static final RegistryHolder<CocktailType> TONIC = register("tonic");
    public static final RegistryHolder<CocktailType> COLADA = register("colada");


    private static @NonNull RegistryHolder<CocktailType> register(String registryKey, EffectData... effects) {
        Supplier<CocktailType> constructure = () -> new CocktailType(ShakenStir.asResource(registryKey), ShakenStir.asResource(registryKey), List.of(effects));
        RegistryHolder<CocktailType> register = new RegistryHolder<>(COCKTAIL_TYPE.register(registryKey, constructure));
        register.warp(constructure);
        return register;
    }

    public static void register(IEventBus modEventBus) {
        COCKTAIL_TYPE.register(modEventBus);
    }

}
