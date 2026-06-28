package io.github.hawah.shakenstir.client.event;

import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.foundation.event.SnsEventBus;
import io.github.hawah.shakenstir.lib.client.render.outliner.Outliner;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import static io.github.hawah.shakenstir.client.event.MC.getLevel;
import static io.github.hawah.shakenstir.client.event.MC.getPlayer;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientGeneralEvents {

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Pre event) {
        if (getLevel() == null) {
            return;
        }
        ShakenStirClient.GLASSWARE_HANDLER.tick();
        ShakenStirClient.SHAKE_HANDLER.tick();
        ShakenStirClient.CABINET_HUD.tick();
        ShakenStirClient.BAR_BUILDER_HANDLER.tick();
        ShakenStirClient.MENU_HUD.tick();
        Outliner.tick();
        if (getPlayer() == null) {
            return;
        }
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        SnsEventBus.initialize("io.github.hawah.shakenstir");
    }

    public static final Identifier HOTBAR_SELECTION_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_selection");

}
