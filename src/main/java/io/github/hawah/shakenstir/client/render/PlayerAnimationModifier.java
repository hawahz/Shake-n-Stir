package io.github.hawah.shakenstir.client.render;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.client.ClientSharedShakeParams;
import io.github.hawah.shakenstir.client.animation.FallPose;
import io.github.hawah.shakenstir.client.animation.MixedShakeAnimation;
import io.github.hawah.shakenstir.client.animation.ShakeAnimation;
import io.github.hawah.shakenstir.content.item.ShakerItem;
import io.github.hawah.shakenstir.foundation.event.EventHandler;
import io.github.hawah.shakenstir.foundation.event.RegisterEvent;
import io.github.hawah.shakenstir.foundation.event.client.ModifyPlayerPoseEvent;
import io.github.hawah.shakenstir.foundation.event.client.RegisterPlayerAnimationEvent;
import io.github.hawah.shakenstir.foundation.utils.ContextKeys;
import io.github.hawah.shakenstir.foundation.utils.ShakeAnimationAccessor;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Ease;
import net.minecraft.util.Mth;

import java.util.Optional;

// TODO: 人工审查 | 2026-07-03 03:30 | Claude Code | 类型:新文件 — 事件系统迁移 + Map化重构
// 概述: PlayerAnimationEventHandler 是动画系统的核心处理器类，包含两个处理器:
//        (1) onRegisterAnimations(RegisterPlayerAnimationEvent)
//            — 响应动画注册事件, 烘焙并注册四个默认动画 (SHAKE/READY/SHAKE_UPPER/FALL)
//              到 Map<Identifier, KeyframeAnimation> 中。
//              原先在 HumanoidModelMixin.init() 中的硬编码烘焙逻辑迁移至此。
//              其他模块可编写额外的 @EventHandler 注册自定义动画。
//        (2) onModifyPlayerPose(ModifyPlayerPoseEvent)
//            — 响应姿态修改事件, 从 Map 中按 Identifier 获取动画并应用到模型。
//              原先在 ThirdPersonArmFixer.onModifyModelPose() 中的逻辑迁移至此。
//              使用 ShakeAnimationAccessor.getAnimation(Identifier) 替代旧版独立 getter。
// 涉及: onRegisterAnimations(), onModifyPlayerPose()
// 原状: (1) HumanoidModelMixin.init() 中四行硬编码 bake (ShakeAnimation.SHAKE.bake 等)
//       (2) ThirdPersonArmFixer.onModifyModelPose() 静态方法 — 与 Mixin 紧耦合
@RegisterEvent
public class PlayerAnimationModifier {
    /** 摇晃动画 (ShakeAnimation.SHAKE) */
    public static final Identifier ANIM_SHAKE = ShakenStir.asResource("shake");
    /** 准备动画 (ShakeAnimation.READY) */
    public static final Identifier ANIM_READY = ShakenStir.asResource("ready");
    /** 上半身摇晃动画 (MixedShakeAnimation.SHAKE_UPPER) */
    public static final Identifier ANIM_SHAKE_UPPER = ShakenStir.asResource("shake_upper");
    /** 倒地动画 (FallPose.POSE) */
    public static final Identifier ANIM_FALL = ShakenStir.asResource("fall");

    // ===== 默认动画注册 =====

    // TODO: 人工审查 | 2026-07-03 03:30 | Claude Code | 类型:逻辑迁移 — 动画烘焙注册
    // 概述: onRegisterAnimations() 响应 RegisterPlayerAnimationEvent，
    //        烘焙并注册四个默认玩家动画到事件的 Map 中。
    //        使用 ShakeAnimationAccessor 中定义的 Identifier 常量作为 key。
    //        其他处理器可通过 @EventHandler(priority = HIGH/LOW) 在默认动画前后注册额外动画。
    // 涉及: 方法 onRegisterAnimations(RegisterPlayerAnimationEvent)
    // 原状: HumanoidModelMixin.init() 中:
    //       shakeNStir$shakeAnimation = ShakeAnimation.SHAKE.bake(root);
    //       shakeNStir$readyAnimation = ShakeAnimation.READY.bake(root);
    //       shakeNStir$shakeUpperAnimation = MixedShakeAnimation.SHAKE_UPPER.bake(root);
    //       shakeNStir$fallPose = FallPose.POSE.bake(root);
    @EventHandler
    public static void onRegisterAnimations(RegisterPlayerAnimationEvent event) {
        if (event.isCanceled()) {
            return;
        }
        var root = event.getRoot();

        // 注册摇晃动画
        event.register(ANIM_SHAKE, ShakeAnimation.SHAKE.bake(root));
        // 注册准备动画
        event.register(ANIM_READY, ShakeAnimation.READY.bake(root));
        // 注册上半身摇晃混合动画
        event.register(ANIM_SHAKE_UPPER, MixedShakeAnimation.SHAKE_UPPER.bake(root));
        // 注册倒地动画
        event.register(ANIM_FALL, FallPose.POSE.bake(root));
    }

    // ===== 姿态修改处理 =====

    // TODO: 人工审查 | 2026-07-03 03:30 | Claude Code | 类型:逻辑迁移 + Map化重构
    // 概述: onModifyPlayerPose() 响应 ModifyPlayerPoseEvent，根据 HumanoidRenderState 状态
    //        从 Map 中获取对应动画并应用到 HumanoidModel。
    //        使用新版 shakeNStir$getAnimation(Identifier) 替代旧版独立 getter。
    //        动画逻辑与 onModifyModelPose 完全一致:
    //        - Shaker 使用: READY (0-10 tick), SHAKE + SHAKE_UPPER (10+ tick, 带过渡)
    //        - FallDown: FALL (easeOutQuart 渐进)
    //        新增事件取消检查 — 高优先级处理器可阻止默认动画执行。
    // 涉及: 方法 onModifyPlayerPose(ModifyPlayerPoseEvent)
    // 原状: ThirdPersonArmFixer.onModifyModelPose(HumanoidRenderState, HumanoidModel<?>)
    //       — 使用旧版 ((ShakeAnimationAccessor) model).shakeNStir$getShakeAnimation() 等
    @EventHandler
    public static void onModifyPlayerPose(ModifyPlayerPoseEvent event) {
        // 如果事件已被更高优先级处理器取消, 跳过默认动画应用
        if (event.isCanceled()) {
            return;
        }

        HumanoidRenderState state = event.getHumanoidRenderState();
        HumanoidModel<?> model = event.getHumanoidModel();

        // 仅处理 AvatarRenderState (玩家/人形实体)
        if (!(state instanceof AvatarRenderState avatarRenderState)) {
            return;
        }

        ShakeAnimationAccessor accessor = (ShakeAnimationAccessor) model;

        // ===== 摇酒器使用动画 =====
        if (state.getMainHandItemStack().getItem() instanceof ShakerItem && state.isUsingItem) {
            float ticksUsingItem = state.ticksUsingItem;
            final float READY_DURATION = 10;
            final float TRANSIT_DURATION = 4;

            // 阶段 1: 准备动画 (0 ~ 10 ticks)
            if (ticksUsingItem < READY_DURATION) {
                KeyframeAnimation readyAnim = accessor.shakeNStir$getAnimation(ANIM_READY);
                if (readyAnim != null) {
                    readyAnim.apply((long) (ticksUsingItem / READY_DURATION * 1000), 1.0F);
                }
                return;
            }

            // 阶段 2: 摇晃动画 (10+ ticks, 带 4-tick 过渡)
            int id = avatarRenderState.id;
            double x = 1 - ClientSharedShakeParams.x(id);
            float y = (float) -(ClientSharedShakeParams.y(id) - 2) / 4;
            double process = (x + 1) / 3;

            if (ticksUsingItem < READY_DURATION + TRANSIT_DURATION) {
                process = Mth.lerp(
                        Ease.outSine((ticksUsingItem - READY_DURATION) / TRANSIT_DURATION),
                        0, process
                );
            }

            KeyframeAnimation shakeAnim = accessor.shakeNStir$getAnimation(ANIM_SHAKE);
            if (shakeAnim != null) {
                shakeAnim.apply((long) (process * 1100), y);
            }
            KeyframeAnimation upperAnim = accessor.shakeNStir$getAnimation(ANIM_SHAKE_UPPER);
            if (upperAnim != null) {
                upperAnim.apply((long) (process * 1100), 1 - y);
            }
        }

        // ===== 倒地动画 =====
        int falldownTick;
        if ((falldownTick = Optional.ofNullable(
                avatarRenderState.getRenderData(ContextKeys.FALLDOWN)).orElse(-1)) > 0) {
            KeyframeAnimation fallAnim = accessor.shakeNStir$getAnimation(ANIM_FALL);
            if (fallAnim != null) {
                model.resetPose();
                fallAnim.apply(
                        0,
                        Ease.outQuart(Mth.clamp(
                                ((AnimationTickHolder.getTicks() - falldownTick) + state.partialTick) / 10,
                                0, 1
                        ))
                );
            }
        }
    }
}
