package io.github.hawah.shakenstir.foundation.mixin;

import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
}
