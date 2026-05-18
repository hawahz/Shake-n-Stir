package io.github.hawah.shakenstir.content.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

@EventBusSubscriber
public abstract class AbstractRemoveHookedMobEffect extends MobEffect {
    protected AbstractRemoveHookedMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public abstract void onEffectRemoved(LivingEntity mob, int amplifier);

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffectInstance() != null && event.getEffect().value() instanceof AbstractRemoveHookedMobEffect effect) {
            effect.onEffectRemoved(event.getEntity(), event.getEffectInstance().getAmplifier());
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() != null && event.getEffectInstance().getEffect().value() instanceof AbstractRemoveHookedMobEffect effect) {
            effect.onEffectRemoved(event.getEntity(), event.getEffectInstance().getAmplifier());
        }
    }
}
