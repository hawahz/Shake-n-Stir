package io.github.hawah.shakenstir.foundation.mixin;

import io.github.hawah.shakenstir.client.render.item.thirdPerson.ThirdPersonArmFixer;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Redirect(method = "setupAnim*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;bobModelPart(Lnet/minecraft/client/model/geom/ModelPart;FF)V", ordinal = 0))
    private void redirectBobRightArm(ModelPart modelPart, float ageInTicks, float scale, S state) {
        if (ThirdPersonArmFixer.shouldApplyArmSwing(state, modelPart, ageInTicks, scale, HumanoidArm.RIGHT)) {
            AnimationUtils.bobModelPart(modelPart, ageInTicks, scale);
        }
    }

    @Redirect(method = "setupAnim*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;bobModelPart(Lnet/minecraft/client/model/geom/ModelPart;FF)V", ordinal = 1))
    private void redirectBobLeftArm(ModelPart modelPart, float ageInTicks, float scale, S state) {
        if (ThirdPersonArmFixer.shouldApplyArmSwing(state, modelPart, ageInTicks, scale, HumanoidArm.LEFT)) {
            AnimationUtils.bobModelPart(modelPart, ageInTicks, scale);
        }
    }
}
