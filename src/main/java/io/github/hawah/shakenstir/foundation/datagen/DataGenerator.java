package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.ShakenStir;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = ShakenStir.MODID)
public class DataGenerator {
    @SubscribeEvent // on the mod event bus
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(ModFluidTagsProvider::new);
        event.createProvider(ModItemTagsProvider::new);
        event.createProvider(ModRecipeProvider.Runner::new);
        event.createProvider(ModModelProvider::new);
        event.createProvider(ModEnUsLangProvider::new);
    }
}
