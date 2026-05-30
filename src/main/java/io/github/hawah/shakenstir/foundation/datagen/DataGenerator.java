package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@EventBusSubscriber(modid = ShakenStir.MODID)
public class DataGenerator {
    @SubscribeEvent // on the mod event bus
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(ModFluidTagsProvider::new);
        event.createProvider(ModItemTagsProvider::new);
        event.createProvider(ModBlockTagsProvider::new);
        event.createProvider(ModDamageTagsProvider::new);
        event.createProvider(ShakeRecipeProvider.Runner::new);
        event.createProvider(ModRecipeProvider.Runner::new);
        event.createProvider(StirRecipeProvider.Runner::new);
        event.createProvider(DistillerRecipeProvider.Runner::new);
        event.createProvider(ModModelProvider::new);
        event.createProvider(ModEnUsLangProvider::new);
        event.createProvider((output, lookupProvider) -> new LootTableProvider(
                output,
                // A set of required table resource locations. These are later verified to be present.
                // It is generally not recommended for mods to validate existence,
                // therefore we pass in an empty set.
                Set.of(),
                // A list of sub provider entries. See below for what values to use here.
                List.of(new LootTableProvider.SubProviderEntry(
                        ModLootTableProvider::new,
                        LootContextParamSets.BLOCK // it makes sense to use BLOCK here
                )),
                 // The registry access
                lookupProvider
        ));
        try {
            ModDatapackGenerator.gatherData(event);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        event.createProvider((output, lookupProvider) -> new AdvancementProvider(
                output, lookupProvider,
                // Add generators here
                List.of(
                        // Add an instance of our generator to the list parameter. This can be done as many times as you want.
                        // Having multiple generators is purely for organization, all functionality can be achieved with a single generator.
                        new ModAdvancementGenerator()
                )
        ));
    }
}
