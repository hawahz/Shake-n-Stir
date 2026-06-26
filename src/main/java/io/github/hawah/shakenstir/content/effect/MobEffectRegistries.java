package io.github.hawah.shakenstir.content.effect;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiFunction;

public class MobEffectRegistries {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, ShakenStir.MODID);
    public static final DeferredHolder<MobEffect, DrunkEffect> DRUNK = register("drunk", DrunkEffect::new, MobEffectCategory.NEUTRAL, 0xFF0000);
    public static final DeferredHolder<MobEffect, FallDownEffect> FALL_DOWN = register("fall_down", FallDownEffect::new, MobEffectCategory.HARMFUL, 0xFF0000);
    public static final DeferredHolder<MobEffect, LemonEffect> LEMON = register("lemon", LemonEffect::new, MobEffectCategory.NEUTRAL, 0xFF0000);
    public static final DeferredHolder<MobEffect, ParalysisEffect> PARALYSIS = register("paralysis", ParalysisEffect::new, MobEffectCategory.BENEFICIAL, 0x659fff);
    public static final DeferredHolder<MobEffect, DummyMobEffect> MISDIRECTION = register("misdirection", DummyMobEffect::new, MobEffectCategory.BENEFICIAL, 0xb6ff00);
    public static final DeferredHolder<MobEffect, DummyMobEffect> MISS_STEP = register("miss_step", DummyMobEffect::new, MobEffectCategory.BENEFICIAL, 0xb6ff00);

    public static <T extends MobEffect> DeferredHolder<MobEffect, T> register(String name, BiFunction<MobEffectCategory, Integer, T> factory, MobEffectCategory category, int color ) {
        return MOB_EFFECTS.register(name, () -> factory.apply(category, color));
    }

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
