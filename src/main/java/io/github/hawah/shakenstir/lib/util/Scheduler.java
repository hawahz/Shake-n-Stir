package io.github.hawah.shakenstir.lib.util;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {
    private static final List<DelayedTask> TASKS = new ArrayList<>();

    public static void schedule(int ticks, Runnable runnable) {
        TASKS.add(new DelayedTask(ticks, runnable));
    }

    public static void tick() {

        TASKS.removeIf(DelayedTask::tick);
    }

    @EventBusSubscriber(Dist.CLIENT)
    static class ClientEvent {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Scheduler.tick();
        }
    }

    @EventBusSubscriber(Dist.DEDICATED_SERVER)
    static class ServerEvent {
        @SubscribeEvent
        public static void onServerTick(ServerTickEvent.Post event) {
            Scheduler.tick();
        }
    }
}
