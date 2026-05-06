package io.github.hawah.shakenstir.foundation.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.hawah.shakenstir.client.ClientEvents;
import io.github.hawah.shakenstir.util.Result;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "turnPlayer", cancellable = true,
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void ShakenStir$turnPlayer(final double d, final CallbackInfo ci,
                                       @Local(ordinal = 4) final double deltaX,
                                       @Local(ordinal = 5) final double deltaYRaw) {
        if (Minecraft.getInstance().player != null && !Minecraft.getInstance().player.isSpectator()) {
            int invertY = Minecraft.getInstance().options.invertMouseX().get()? -1 : 1;
            final double finalDeltaY = deltaYRaw * invertY;
            final Result status = ClientEvents.onMouseMove(deltaX, finalDeltaY);
            if (status.cancelled()) {
                ci.cancel();
            }
        }
    }
}
