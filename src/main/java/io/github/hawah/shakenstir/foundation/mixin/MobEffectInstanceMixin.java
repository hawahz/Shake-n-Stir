package io.github.hawah.shakenstir.foundation.mixin;

import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceMixin {

    @Accessor("duration")
    abstract void setDuration(int duration);

    @Shadow
    public abstract int getDuration();

    @Accessor("amplifier")
    abstract void setAmplifier(int amplifier);

    @Shadow
    public abstract int getAmplifier();

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void ShakenStir$update(MobEffectInstance takeOver, CallbackInfoReturnable<Boolean> cir) {
        if (!takeOver.getEffect().is(MobEffectRegistries.DRUNK.getKey())) {
            return;
        }
        int duration = Math.max(takeOver.getDuration(), getDuration());
        ((MobEffectInstanceMixin) (Object) takeOver).setDuration(duration);
        setDuration(duration);
        int amplifier = takeOver.getAmplifier() + getAmplifier();
        if (amplifier == takeOver.getAmplifier() || amplifier == getAmplifier()) {
            amplifier ++;
        }
        ((MobEffectInstanceMixin) (Object) takeOver).setAmplifier(amplifier);
        this.setAmplifier(amplifier);
        cir.cancel();
        cir.setReturnValue(true);
    }
}
