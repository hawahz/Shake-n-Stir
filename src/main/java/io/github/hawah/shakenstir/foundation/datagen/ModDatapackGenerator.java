package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.damageType.SnsDamageType;
import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import io.github.hawah.shakenstir.foundation.datapack.DatapackRegistries;
import io.github.hawah.shakenstir.foundation.datapack.EffectData;
import io.github.hawah.shakenstir.foundation.datapack.IngredientData;
import io.github.hawah.shakenstir.foundation.datapack.cocktaileType.CocktailType;
import io.github.hawah.shakenstir.foundation.datapack.spirit.SpiritData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ModDatapackGenerator {

    public static void gatherData(GatherDataEvent event) throws ExecutionException, InterruptedException {
        HolderLookup.Provider provider = event.getLookupProvider().get();
        event.createDatapackRegistryObjects(new RegistrySetBuilder()
                        // Add a datapack builtin entry provider for damage types. If this lambda becomes longer,
                        // this should probably be extracted into a separate method for the sake of readability.
                        .add(Registries.DAMAGE_TYPE, bootstrap -> {
                            // Use new DamageType() to create an in-code representation of a damage type.
                            // The parameters map to the values of the JSON file, in the order seen above.
                            // All parameters except for the message id and the exhaustion value are optional.
                            bootstrap.register(SnsDamageType.PARALYSIS, new DamageType(SnsDamageType.PARALYSIS.identifier().toString(),
                                    DamageScaling.ALWAYS,
                                    0.1f,
                                    DamageEffects.HURT,
                                    DeathMessageType.DEFAULT)
                            );
                        })
                        .add(DatapackRegistries.SPIRIT_REGISTRY_KEY, bootstrap  -> {
                            bootstrap.register(
                                    spiritKey("example"),
                                    new SpiritData(
                                            FluidRegistries.BUBBLE_SOURCE_FLUID_BLOCK,
                                            EffectData.of(MobEffects.STRENGTH, List.of(1, 2))
                                    )
                            );
                        })
                        .add(
                                DatapackRegistries.COCKTAIL_REGISTRY_KEY, bootstrap -> {
                                    bootstrap.register(
                                            cocktailKey("example"),
                                            new CocktailType(
                                                    ShakenStir.asResource("example"),
                                                    List.of(new EffectData(
                                                            MobEffects.JUMP_BOOST,
                                                            MobEffects.JUMP_BOOST,
                                                            List.of(new EffectData.LevelMapDuration(1, 60)),
                                                            List.of())
                                                    )
                                            )
                                    );
                                }
                        )
                        .add(
                                DatapackRegistries.INGREDIENT_REGISTRY_KEY, bootstrap -> {
                                    bootstrap.register(
                                            ingredientKey("example"),
                                            new IngredientData(
                                                    Ingredient.of(provider.getOrThrow(Tags.Items.ARMORS)),
                                                    EffectData.of(MobEffects.STRENGTH, List.of(1, 2))
                                            )
                                    );
                                }
                        )
                // Add datapack providers for other datapack entries, if applicable.
        );
    }

    public static ResourceKey<SpiritData> spiritKey(String name) {
        return ResourceKey.create(
                DatapackRegistries.SPIRIT_REGISTRY_KEY,
                ShakenStir.asResource(name)
        );
    }
    public static ResourceKey<CocktailType> cocktailKey(String name) {
        return ResourceKey.create(
                DatapackRegistries.COCKTAIL_REGISTRY_KEY,
                ShakenStir.asResource(name)
        );
    }

    public static ResourceKey<IngredientData> ingredientKey(String name) {
        return ResourceKey.create(
                DatapackRegistries.INGREDIENT_REGISTRY_KEY,
                ShakenStir.asResource(name)
        );
    }
}
