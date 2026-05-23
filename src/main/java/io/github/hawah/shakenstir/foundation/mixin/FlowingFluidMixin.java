package io.github.hawah.shakenstir.foundation.mixin;

import io.github.hawah.shakenstir.foundation.tags.SnsBlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowingFluid.class)
public class FlowingFluidMixin {

    @Inject(method = "canHoldAnyFluid", at = @At("RETURN"), cancellable = true)
    private static void canHoldAnyFluid(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            boolean flag = !state.is(SnsBlockTags.BLOCKING_FLUID);
            cir.setReturnValue(flag);
        }
    }
}
