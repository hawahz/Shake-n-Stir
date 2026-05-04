package io.github.hawah.shakenstir.client;

import io.github.hawah.shakenstir.ShakenStirClient;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent.AfterTranslucentParticles event) {
        ShakenStirClient.TIMER_NORMAL.warp(Minecraft.getInstance().getDeltaTracker());
    }
}
