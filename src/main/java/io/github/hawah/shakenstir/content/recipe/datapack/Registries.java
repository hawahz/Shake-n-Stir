package io.github.hawah.shakenstir.content.recipe.datapack;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.recipe.datapack.cocktaileType.CocktailType;
import io.github.hawah.shakenstir.content.recipe.datapack.spirit.SpiritData;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber
public class Registries {

    public static final ResourceKey<Registry<SpiritData>> SPIRIT_REGISTRY_KEY = ResourceKey.createRegistryKey(ShakenStir.asResource("spirit"));
    public static final ResourceKey<Registry<IngredientData>> INGREDIENT_REGISTRY_KEY = ResourceKey.createRegistryKey(ShakenStir.asResource("ingredient"));
    public static final ResourceKey<Registry<CocktailType>> COCKTAIL_REGISTRY_KEY = ResourceKey.createRegistryKey(ShakenStir.asResource("cocktail"));
    public static final ResourceKey<Registry<DrinkData>> DRINK_REGISTRY_KEY = ResourceKey.createRegistryKey(ShakenStir.asResource("drink"));

//    public static final Registry<SpiritData> SPIRIT_REGISTRY = new RegistryBuilder<>(SPIRIT_REGISTRY_KEY).sync(true).defaultKey(ShakenStir.asResource("empty")).create();
//    public static final Registry<IngredientData> INGREDIENT_REGISTRY = new RegistryBuilder<>(INGREDIENT_REGISTRY_KEY).sync(true).defaultKey(ShakenStir.asResource("empty")).create();
//    public static final Registry<CocktailType> COCKTAIL_REGISTRY = new RegistryBuilder<>(COCKTAIL_REGISTRY_KEY).sync(true).defaultKey(ShakenStir.asResource("empty")).create();
//    public static final Registry<DrinkData> DRINK_REGISTRY = new RegistryBuilder<>(DRINK_REGISTRY_KEY).sync(true).defaultKey(ShakenStir.asResource("empty")).create();

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
//
//    @SubscribeEvent
//    public static void registerRegistries(NewRegistryEvent event) {
//        event.register(SPIRIT_REGISTRY);
//        event.register(INGREDIENT_REGISTRY);
//        event.register(COCKTAIL_REGISTRY);
//        event.register(DRINK_REGISTRY);
//    }
}
