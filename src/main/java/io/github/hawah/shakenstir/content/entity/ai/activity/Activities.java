package io.github.hawah.shakenstir.content.entity.ai.activity;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Activities {
    public static final DeferredRegister<Activity> ACTIVITY = DeferredRegister.create(Registries.ACTIVITY, ShakenStir.MODID);
    public static final DeferredHolder<Activity, Activity> WORK_IDLE = register("work_idle");
    public static final DeferredHolder<Activity, Activity> SHAKING = register("shaking");

    public static void register(IEventBus modEventBus) {
        ACTIVITY.register(modEventBus);
    }

    public static DeferredHolder<Activity, Activity> register(String name) {
        return ACTIVITY.register(name, () -> new Activity(name));
    }
}
