package io.github.hawah.shakenstir.client;

import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import io.github.hawah.shakenstir.foundation.networking.ServerboundEntityFallPacket;
import io.github.hawah.shakenstir.lib.networking.Networking;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(Dist.CLIENT)
public class EntityStepUpEvent {
    public static Map<UUID, Double> oldY = new HashMap<>();

    @SubscribeEvent
    public static void onEntityTickPre(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            oldY.put(livingEntity.getUUID(), livingEntity.position().y());
        }
    }

    @SubscribeEvent
    public static void onEntityTickPost(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            UUID uuid = livingEntity.getUUID();
            Double previousY = oldY.remove(uuid);
            if (previousY != null
                    && (!(livingEntity instanceof Player player) || !player.getAbilities().flying)
                    && livingEntity.position().y() - previousY > 0) {
                onEntityStepUp(livingEntity, livingEntity.position().y() - previousY);
            }
        }
    }

    public static void onEntityStepUp(LivingEntity livingEntity, double stepUpDistance) {
        MobEffectInstance effect = livingEntity.getEffect(MobEffectRegistries.DRUNK);

        double speed = livingEntity.getDeltaMovement().horizontalDistance();
        if (effect != null && effect.getAmplifier() > 5 && speed > ((effect.getAmplifier()) > 8? 0.10: 0.12)) {
            double chance = 0.1 * effect.getAmplifier() * (Math.abs(stepUpDistance - 0.5) + 0.1);
            if (livingEntity.level().getRandom().nextDouble() < chance && !livingEntity.hasEffect(MobEffectRegistries.FALL_DOWN)) {
                Networking.sendToServer(new ServerboundEntityFallPacket(livingEntity.getUUID()));
            }
        }
    }
}
