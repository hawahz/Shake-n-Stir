package io.github.hawah.shakenstir.foundation.mixin;

import io.github.hawah.shakenstir.client.animation.MixedShakeAnimation;
import io.github.hawah.shakenstir.client.animation.ShakeAnimation;
import io.github.hawah.shakenstir.client.render.item.thirdPerson.ThirdPersonArmFixer;
import io.github.hawah.shakenstir.foundation.utils.ShakeAnimationAccessor;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<S extends HumanoidRenderState> implements ShakeAnimationAccessor {

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

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("RETURN"))
    private void setUpAnim(S state, CallbackInfo ci) {
        ThirdPersonArmFixer.onModifyModelPose(state, (HumanoidModel<?>) (Object)this);
    }

    @Unique
    public KeyframeAnimation shakeNStir$shakeAnimation;

    @Unique
    public KeyframeAnimation shakeNStir$readyAnimation;

    @Unique
    public KeyframeAnimation shakeNStir$shakeUpperAnimation;

    @Inject(method = "<init>(Lnet/minecraft/client/model/geom/ModelPart;Ljava/util/function/Function;)V", at = @At("RETURN"))
    private void init(ModelPart root, Function<Identifier, RenderType> renderType, CallbackInfo ci) {
        shakeNStir$shakeAnimation = ShakeAnimation.SHAKE.bake(root);
        shakeNStir$readyAnimation = ShakeAnimation.READY.bake(root);
        shakeNStir$shakeUpperAnimation = MixedShakeAnimation.SHAKE_UPPER.bake(root);
    }

    @Override
    public KeyframeAnimation shakeNStir$getShakeAnimation() {
        return shakeNStir$shakeAnimation;
    }

    @Override
    public KeyframeAnimation shakeNStir$getReadyAnimation() {
        return shakeNStir$readyAnimation;
    }

    @Override
    public KeyframeAnimation shakeNStir$getShakeUpperAnimation() {
        return shakeNStir$shakeUpperAnimation;
    }
}
