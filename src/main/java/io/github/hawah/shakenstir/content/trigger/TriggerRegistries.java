package io.github.hawah.shakenstir.content.trigger;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TriggerRegistries {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, ShakenStir.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> SHAKE_BUBBLE_EXPLODE =
            TRIGGERS.register("shake_bubble_explode", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> SHAKER_OVERTURN =
            TRIGGERS.register("shaker_overturn", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> FIRST_DRUNK =
            TRIGGERS.register("first_drunk", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> DRUNK_HEAVY =
            TRIGGERS.register("drunk_heavy", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> FIRST_FALL_BY_DRUNK =
            TRIGGERS.register("first_fall_by_drunk", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> FIRST_HIT_DUE_TO_LEMON =
            TRIGGERS.register("first_hit_due_to_lemon", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> DIED_BY_DISCOVERING_PARALYSIS =
            TRIGGERS.register("died_by_discovering_paralysis", SimpleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SimpleTrigger> PROTECTED_BY_PARALYSIS =
            TRIGGERS.register("protected_by_paralysis", SimpleTrigger::new);

    public static void register(IEventBus eventBus) {
        TRIGGERS.register(eventBus);
    }
}