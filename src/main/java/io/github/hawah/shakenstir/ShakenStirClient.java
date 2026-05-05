package io.github.hawah.shakenstir;

import io.github.hawah.shakenstir.client.hanlder.ShakeHandler;
import io.github.hawah.shakenstir.lib.client.utils.TimerWarper;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ShakenStir.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = ShakenStir.MODID, value = Dist.CLIENT)
public class ShakenStirClient {
    public static final TimerWarper TIMER_NORMAL = new TimerWarper();
    public static final ShakeHandler SHAKE_HANDLER = new ShakeHandler();
    public static final float ANI_DELTAF = 0.5F;

    public ShakenStirClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        ShakenStir.LOGGER.info("HELLO FROM CLIENT SETUP");
        ShakenStir.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
