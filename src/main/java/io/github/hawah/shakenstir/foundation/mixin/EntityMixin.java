package io.github.hawah.shakenstir.foundation.mixin;

import io.github.hawah.shakenstir.content.dataAttachment.DataAttachmentTypeRegistries;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "getEyeHeight()F", at = @At("RETURN"), cancellable = true)
    public void shakeNStir$getEyeHeight(CallbackInfoReturnable<Float> cir) {
        if (((Entity) (Object) this).getData(DataAttachmentTypeRegistries.FALL_DOWN) > 0) {
            cir.setReturnValue(0.4F);
        }
    }
}
