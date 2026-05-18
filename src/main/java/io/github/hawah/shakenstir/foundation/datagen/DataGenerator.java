package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.damageType.SnsDamageType;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = ShakenStir.MODID)
public class DataGenerator {
    @SubscribeEvent // on the mod event bus
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(ModFluidTagsProvider::new);
        event.createProvider(ModItemTagsProvider::new);
        event.createProvider(ModDamageTagsProvider::new);
        event.createProvider(ShakeRecipeProvider.Runner::new);
        event.createProvider(StirRecipeProvider.Runner::new);
        event.createProvider(ModModelProvider::new);
        event.createProvider(ModEnUsLangProvider::new);
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
                // Add datapack providers for other datapack entries, if applicable.
        );
    }
}
