package io.github.hawah.shakenstir.foundation.mixin;

import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEffect.class)
public class MobEffectMixin {

    @Inject(method = "applyInstantenousEffect", at = @At(value = "HEAD"), cancellable = true)
    public void applyInstantenousEffect(ServerLevel level,
                                        Entity source,
                                        Entity owner,
                                        LivingEntity mob,
                                        int amplification,
                                        double scale,
                                        CallbackInfo ci) {
        if (mob.hasEffect(MobEffectRegistries.PARALYSIS)) {
            ci.cancel();
            mob.addEffect(new MobEffectInstance(Holder.direct((MobEffect) (Object) this), 1, 0, false, false, false));
        }
    }
}
