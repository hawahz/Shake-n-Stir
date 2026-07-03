package io.github.hawah.shakenstir.foundation.event.client;

import io.github.hawah.shakenstir.foundation.event.AbstractSnsEvent;
import io.github.hawah.shakenstir.foundation.event.Side;
import io.github.hawah.shakenstir.foundation.event.SnsRegisterEvent;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

// TODO: 人工审查 | 2026-07-03 03:30 | Claude Code | 类型:新文件 — 事件系统迁移
// 概述: ModifyPlayerPoseEvent 在 HumanoidModel.setupAnim() 完成后发布，
//        允许处理器修改玩家模型的动画姿态。
//        替代 HumanoidModelMixin 中直接调用 ThirdPersonArmFixer.onModifyModelPose() 的硬编码方式。
//        事件携带 HumanoidRenderState (含动画状态、物品使用状态等) 和 HumanoidModel (含动画 Map)。
//        继承 AbstractSnsEvent → 可取消: 高优先级处理器可通过 setCanceled(true) 阻止默认动画执行。
//        仅客户端生效 (@SnsRegisterEvent(Side.CLIENT))。
// 涉及: 新增 ModifyPlayerPoseEvent 类
// 原状: 无 — HumanoidModelMixin.setUpAnim() 直接调用:
//       ThirdPersonArmFixer.onModifyModelPose(state, (HumanoidModel<?>) (Object)this);
//       — 无事件解耦, 无取消机制, 不可扩展
@SnsRegisterEvent(Side.CLIENT)
public class ModifyPlayerPoseEvent extends AbstractSnsEvent {
    private final HumanoidRenderState humanoidRenderState;
    private final HumanoidModel<?> humanoidModel;

    public ModifyPlayerPoseEvent(HumanoidRenderState humanoidRenderState, HumanoidModel<?> humanoidModel) {
        this.humanoidRenderState = humanoidRenderState;
        this.humanoidModel = humanoidModel;
    }

    /**
     * 获取当前的人形渲染状态。
     * 包含动画状态、物品使用状态 (isUsingItem, ticksUsingItem)、partialTick、fall down tick 等。
     */
    public HumanoidRenderState getHumanoidRenderState() {
        return humanoidRenderState;
    }

    /**
     * 获取当前的人形模型实例。
     * 可通过 {@code model instanceof ShakeAnimationAccessor accessor}
     * 获取 Map<Identifier, KeyframeAnimation> 并按 key 应用动画。
     */
    public HumanoidModel<?> getHumanoidModel() {
        return humanoidModel;
    }
}
