package io.github.hawah.shakenstir.client.render.item.thirdPerson;

import io.github.hawah.shakenstir.client.ClientSharedShakeParams;
import io.github.hawah.shakenstir.client.render.PlayerAnimationModifier;
import io.github.hawah.shakenstir.client.render.entity.BartenderRenderState;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.item.ShakerItem;
import io.github.hawah.shakenstir.foundation.utils.ContextKeys;
import io.github.hawah.shakenstir.foundation.utils.ShakeAnimationAccessor;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Ease;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

import java.util.Optional;

public class ThirdPersonArmFixer {

    // TODO: 人工审查 | 2026-07-03 03:30 | Claude Code | 类型:已废弃 — 逻辑已迁移至事件系统 + Map化重构
    // 概述: onModifyModelPose() 的逻辑已完整迁移至 PlayerAnimationEventHandler.onModifyPlayerPose()
    //        (响应 ModifyPlayerPoseEvent)。HumanoidModelMixin 现通过 SnsEvents.post() 发布事件。
    //        此方法内部已更新为使用新版 Map 接口 (shakeNStir$getAnimation(Identifier))，
    //        替代旧版独立 getter 方法, 确保编译兼容。
    //        建议: 确认无外部引用后删除此方法及不再需要的 import。
    // 涉及: onModifyModelPose() — 标记 @Deprecated, 内部使用 Map 接口
    // 原状: 使用 ((ShakeAnimationAccessor) model).shakeNStir$getReadyAnimation() 等旧版 getter；
    //       由 HumanoidModelMixin.setUpAnim() 直接调用, 与 Mixin 紧耦合
    @Deprecated
    public static void onModifyModelPose(HumanoidRenderState state, HumanoidModel<?> model) {
        if (!(state instanceof AvatarRenderState avatarRenderState)) {
            return;
        }
        ShakeAnimationAccessor accessor = (ShakeAnimationAccessor) model;

        if (state.getMainHandItemStack().getItem() instanceof ShakerItem && state.isUsingItem) {
            float ticksUsingItem = state.ticksUsingItem;
            final float READY_DURATION = 10;
            final float TRANSIT_DURATION = 4;
            if (ticksUsingItem < READY_DURATION) {
                KeyframeAnimation readyAnim = accessor.shakeNStir$getAnimation(PlayerAnimationModifier.ANIM_READY);
                if (readyAnim != null) {
                    readyAnim.apply((long) (ticksUsingItem / READY_DURATION * 1000), 1.0F);
                }
                return;
            }
            int id = avatarRenderState.id;
            double x = 1 - ClientSharedShakeParams.x(id);
            float y = (float) -(ClientSharedShakeParams.y(id) - 2) / 4;
            double process = (x + 1) / 3;
            if (ticksUsingItem < READY_DURATION + TRANSIT_DURATION) {
                process = Mth.lerp(Ease.outSine((ticksUsingItem - READY_DURATION) / TRANSIT_DURATION), 0, process);
            }
            KeyframeAnimation shakeAnim = accessor.shakeNStir$getAnimation(PlayerAnimationModifier.ANIM_SHAKE);
            if (shakeAnim != null) {
                shakeAnim.apply((long) (process * 1100), y);
            }
            KeyframeAnimation upperAnim = accessor.shakeNStir$getAnimation(PlayerAnimationModifier.ANIM_SHAKE_UPPER);
            if (upperAnim != null) {
                upperAnim.apply((long) (process * 1100), 1 - y);
            }
        }
        int falldownTick;
        if ((falldownTick = Optional.ofNullable(avatarRenderState.getRenderData(ContextKeys.FALLDOWN)).orElse(-1)) > 0) {
            KeyframeAnimation fallAnim = accessor.shakeNStir$getAnimation(PlayerAnimationModifier.ANIM_FALL);
            if (fallAnim != null) {
                model.resetPose();
                fallAnim.apply(0, Ease.outQuart(Mth.clamp(((AnimationTickHolder.getTicks() - falldownTick) + state.partialTick) / 10, 0, 1)));
            }
        }
    }

    public static boolean shouldApplyArmSwing(HumanoidRenderState state,
                                              ModelPart part,
                                              float ageInTicks,
                                              float multiplier,
                                              HumanoidArm arm) {
        return !(state.getMainHandItemStack().is(ItemRegistries.SHAKER) && state.isUsingItem)
                && !(state instanceof BartenderRenderState bartenderRenderState
                && bartenderRenderState.animState != BartenderEntity.AnimState.DEFAULT);
    }
}
