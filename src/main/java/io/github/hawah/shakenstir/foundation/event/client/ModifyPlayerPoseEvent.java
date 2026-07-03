package io.github.hawah.shakenstir.foundation.event.client;

import io.github.hawah.shakenstir.foundation.event.AbstractSnsEvent;
import io.github.hawah.shakenstir.foundation.event.Side;
import io.github.hawah.shakenstir.foundation.event.SnsRegisterEvent;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

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
