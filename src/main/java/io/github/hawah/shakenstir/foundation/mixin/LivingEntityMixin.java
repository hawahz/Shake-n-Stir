package io.github.hawah.shakenstir.foundation.mixin;

import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.effect.ParalysisEffect;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract ItemStack getUseItem();
    @Inject(method = "canFreeze", at = @At("RETURN"), cancellable = true)
    public void canFreeze(CallbackInfoReturnable<Boolean> cir) {
        boolean defaultReturnValue = cir.getReturnValue();
        if (defaultReturnValue) {
            return;
        }
        if (getUseItem() != null && getUseItem().has(DataComponentTypeRegistries.SHAKING)) {
            cir.setReturnValue(true);
        }
    }

    @Final
    @Shadow
    private Map<Holder<MobEffect>, MobEffectInstance> activeEffects;

    @Inject(method = "tickEffects", at = @At("HEAD"), cancellable = true)
    public void tickEffects(CallbackInfo ci) {
        if (ParalysisEffect.injectEffectTick((LivingEntity) (Object) this, activeEffects)) {
            ci.cancel();
        }
    }

}
