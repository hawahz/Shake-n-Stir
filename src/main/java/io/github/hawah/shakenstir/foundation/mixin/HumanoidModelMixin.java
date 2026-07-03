package io.github.hawah.shakenstir.foundation.mixin;

import io.github.hawah.shakenstir.client.render.item.thirdPerson.ThirdPersonArmFixer;
import io.github.hawah.shakenstir.foundation.event.SnsEvents;
import io.github.hawah.shakenstir.foundation.event.client.ModifyPlayerPoseEvent;
import io.github.hawah.shakenstir.foundation.event.client.RegisterPlayerAnimationEvent;
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

import java.util.Collections;
import java.util.Map;
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
        SnsEvents.post(new ModifyPlayerPoseEvent(state, (HumanoidModel<?>) (Object) this));
    }

    @Unique
    private Map<Identifier, KeyframeAnimation> shakeNStir$animations;

    @Inject(method = "<init>(Lnet/minecraft/client/model/geom/ModelPart;Ljava/util/function/Function;)V", at = @At("RETURN"))
    private void init(ModelPart root, Function<Identifier, RenderType> renderType, CallbackInfo ci) {
        RegisterPlayerAnimationEvent event = new RegisterPlayerAnimationEvent(root);
        SnsEvents.post(event);
        shakeNStir$animations = Collections.unmodifiableMap(event.getAnimationsInternal());
    }

    // ===== ShakeAnimationAccessor 实现 =====

    @Override
    public Map<Identifier, KeyframeAnimation> shakeNStir$getAnimations() {
        return shakeNStir$animations;
    }
}
