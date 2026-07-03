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

// TODO: 人工审查 | 2026-07-03 03:30 | Claude Code | 类型:Map化重构 — 事件驱动的动画注册
// 概述: HumanoidModelMixin 从四个硬编码 @Unique KeyframeAnimation 字段重构为
//        单一 Map<Identifier, KeyframeAnimation>，动画通过事件系统注册。
//        变更摘要:
//        (1) 四个独立字段 (shake/ready/shakeUpper/fallPose) → 一个 Map
//        (2) init() 不再直接烘焙动画 (ShakeAnimation.SHAKE.bake 等四行)，
//            改为发布 RegisterPlayerAnimationEvent，由处理器负责烘焙和注册
//        (3) 四个 @Override getter 方法移除，ShakeAnimationAccessor 接口的 default 实现自动委托到 Map
//        (4) setUpAnim() 通过 SnsEvents.post(ModifyPlayerPoseEvent) 发布姿态修改事件
//        (5) 移除不再需要的 import: FallPose, MixedShakeAnimation, ShakeAnimation
// 涉及: init() — 动画烘焙改为事件驱动; 新增 shakeNStir$getAnimations(); setUpAnim() — 事件发布
// 原状: 四个 @Unique 字段 + 四个 @Override getter + init() 四行硬编码 bake
@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<S extends HumanoidRenderState> implements ShakeAnimationAccessor {

    // TODO: 人工审查 | 2026-07-03 03:30 | Claude Code | 类型:手臂摆动重定向 — 保留 @Redirect
    // 概述: redirectBobRightArm() / redirectBobLeftArm() 通过 @Redirect 重定向手臂摆动动画。
    //        当手持 Shaker 并使用中, 或 Bartender 不在 DEFAULT 状态时跳过手臂摆动。
    //        此部分仍需 @Redirect (字节码级别注入), 暂未迁移至事件系统。
    // 涉及: redirectBobRightArm(), redirectBobLeftArm()
    // 原状: 同上, 无注释
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

    // TODO: 人工审查 | 2026-07-03 03:30 | Claude Code | 类型:事件系统迁移 — 姿态修改事件发布
    // 概述: setUpAnim() 在 HumanoidModel.setupAnim() RETURN 时发布 ModifyPlayerPoseEvent，
    //        替代原先直接调用 ThirdPersonArmFixer.onModifyModelPose()。
    //        动画逻辑由 PlayerAnimationEventHandler.onModifyPlayerPose() 处理器响应。
    // 涉及: setUpAnim() — 从直接方法调用改为事件发布
    // 原状: ThirdPersonArmFixer.onModifyModelPose(state, (HumanoidModel<?>) (Object)this);
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("RETURN"))
    private void setUpAnim(S state, CallbackInfo ci) {
        SnsEvents.post(new ModifyPlayerPoseEvent(state, (HumanoidModel<?>) (Object) this));
    }

    // ===== 动画 Map — 替代原先四个 @Unique KeyframeAnimation 字段 =====

    // TODO: 人工审查 | 2026-07-03 03:30 | Claude Code | 类型:Map化重构 — 统一动画存储
    // 概述: 原先四个 @Unique KeyframeAnimation 字段合并为单一 Map<Identifier, KeyframeAnimation>。
    //        通过 ShakeAnimationAccessor 暴露 getAnimations() / getAnimation(Identifier) 方法。
    //        动画在 init() 中通过 RegisterPlayerAnimationEvent 事件注册。
    // 涉及: shakeNStir$animations 字段 — 替代四个旧字段
    // 原状: @Unique public KeyframeAnimation shakeNStir$shakeAnimation / readyAnimation /
    //       shakeUpperAnimation / fallPose — 四个独立字段, 无法扩展
    @Unique
    private Map<Identifier, KeyframeAnimation> shakeNStir$animations;

    // TODO: 人工审查 | 2026-07-03 03:30 | Claude Code | 类型:Map化重构 — 事件驱动的动画注册
    // 概述: init() 不再直接调用 ShakeAnimation.SHAKE.bake(root) 等四行硬编码烘焙。
    //        创建空 Map → 发布 RegisterPlayerAnimationEvent(root) →
    //        处理器 (PlayerAnimationEventHandler) 负责烘焙并注册动画 → 存储不可变 Map。
    //        移除导入: FallPose, MixedShakeAnimation, ShakeAnimation (不再需要)
    // 涉及: init() — 动画烘焙从硬编码改为事件驱动
    // 原状: shakeNStir$shakeAnimation = ShakeAnimation.SHAKE.bake(root);
    //       shakeNStir$readyAnimation = ShakeAnimation.READY.bake(root);
    //       shakeNStir$shakeUpperAnimation = MixedShakeAnimation.SHAKE_UPPER.bake(root);
    //       shakeNStir$fallPose = FallPose.POSE.bake(root);
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

    // 注意: shakeNStir$getShakeAnimation() / getReadyAnimation() / getShakeUpperAnimation() /
    // getFallAnimation() 的 @Override 已移除。ShakeAnimationAccessor 接口现提供 default 实现,
    // 自动委托到 shakeNStir$getAnimations().get(ANIM_XXX)，无需子类重复实现。
}
