package io.github.hawah.shakenstir.client;

import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import io.github.hawah.shakenstir.foundation.networking.ServerboundEntityFallPacket;
import io.github.hawah.shakenstir.lib.networking.Networking;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(Dist.CLIENT)
public class EntityStepUpEvent {
    public static Map<LivingEntity, Double> oldY = new HashMap<>();

    @SubscribeEvent
    public static void onEntityTickPre(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            oldY.put(livingEntity, livingEntity.position().y());
        }
    }

    @SubscribeEvent
    public static void onEntityTickPost(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            if (oldY.containsKey(livingEntity) && livingEntity.position().y() - oldY.get(livingEntity) > 0) {
                onEntityStepUp(livingEntity, livingEntity.position().y() - oldY.get(livingEntity));
            }
            oldY.remove(livingEntity);
        }
    }

    public static void onEntityStepUp(LivingEntity livingEntity, double stepUpDistance) {
        MobEffectInstance effect = livingEntity.getEffect(MobEffectRegistries.DRUNK);
        if (effect != null) {
            double chance = 0.1 * effect.getAmplifier() * (Math.abs(stepUpDistance - 0.5) + 0.1);
            if (livingEntity.level().getRandom().nextDouble() < chance && !livingEntity.hasEffect(MobEffectRegistries.FALL_DOWN)) {
                Networking.sendToServer(new ServerboundEntityFallPacket(livingEntity.getUUID()));
            }
        }
    }
}
