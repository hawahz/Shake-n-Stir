package io.github.hawah.shakenstir.foundation.datapack;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.foundation.datapack.cocktaileType.CocktailType;
import io.github.hawah.shakenstir.foundation.datapack.spirit.SpiritData;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber
public class DatapackRegistries {

    public static final ResourceKey<Registry<SpiritData>> SPIRIT_REGISTRY_KEY = ResourceKey.createRegistryKey(ShakenStir.asResource("spirit"));
    public static final ResourceKey<Registry<IngredientData>> INGREDIENT_REGISTRY_KEY = ResourceKey.createRegistryKey(ShakenStir.asResource("ingredient"));
    public static final ResourceKey<Registry<CocktailType>> COCKTAIL_REGISTRY_KEY = ResourceKey.createRegistryKey(ShakenStir.asResource("cocktail"));
    public static final ResourceKey<Registry<DrinkData>> DRINK_REGISTRY_KEY = ResourceKey.createRegistryKey(ShakenStir.asResource("drink"));

    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                SPIRIT_REGISTRY_KEY,
                SpiritData.CODEC,
                SpiritData.CODEC
        );
        event.dataPackRegistry(
                INGREDIENT_REGISTRY_KEY,
                IngredientData.CODEC,
                IngredientData.CODEC
        );
        event.dataPackRegistry(
                COCKTAIL_REGISTRY_KEY,
                CocktailType.CODEC,
                CocktailType.CODEC
        );
        event.dataPackRegistry(
                DRINK_REGISTRY_KEY,
                DrinkData.CODEC,
                DrinkData.CODEC
        );
    }
}
