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
