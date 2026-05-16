package io.github.hawah.shakenstir.foundation.mixin;

import io.github.hawah.shakenstir.client.render.item.thirdPerson.ThirdPersonArmFixer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<S extends HumanoidRenderState> {

    @Final
    @Shadow
    public ModelPart rightArm;

    @Final
    @Shadow
    public ModelPart leftArm;

    @Inject(method = "poseRightArm", at = @At("RETURN"))
    public void poseRightArm(S state, CallbackInfo ci) {
        ThirdPersonArmFixer.onRenderRightArm(rightArm, state);
    }

    @Inject(method = "poseLeftArm", at = @At("RETURN"))
    public void poseLeftArm(S state, CallbackInfo ci) {
        ThirdPersonArmFixer.onRenderLeftArm(leftArm, state);
    }
}
