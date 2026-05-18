package io.github.hawah.shakenstir.foundation.events;

import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class MixinEvents {
    public static void onEntityStepUp(LivingEntity livingEntity) {
        MobEffectInstance effect = livingEntity.getEffect(MobEffectRegistries.DRUNK);
        if (effect != null && effect.getAmplifier() > 5) {
            if (livingEntity.level().getRandom().nextDouble() < 0.1 * effect.getAmplifier() && !livingEntity.hasEffect(MobEffectRegistries.FALL_DOWN) && livingEntity.getDeltaMovement().horizontalDistanceSqr() != 0) {
                //livingEntity.addEffect(new MobEffectInstance(MobEffectRegistries.FALL_DOWN, 100));
            }
        }
    }
}
