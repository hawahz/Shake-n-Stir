package io.github.hawah.shakenstir.foundation.event.client;

import io.github.hawah.shakenstir.foundation.event.AbstractSnsEvent;
import io.github.hawah.shakenstir.foundation.event.Side;
import io.github.hawah.shakenstir.foundation.event.SnsRegisterEvent;

// TODO: 人工审查 | 2026-06-29 | IDE Linter | 类型:参数适配
// 概述: @SnsRegisterEvent 参数从 side() 改为 value()。
//        @SnsRegisterEvent(Side.CLIENT) 使用 value= 简写，等价于原 @SnsRegisterEvent(side=Side.CLIENT)。
// 涉及: 注解语法适配; import 新增 Side (Linter 自动添加)
// 原状: @SnsRegisterEvent(side = Side.CLIENT)
@SnsRegisterEvent(Side.CLIENT)
public class LevelMouseMoveEvent extends AbstractSnsEvent {
    private double yaw;
    private double pitch;

    public LevelMouseMoveEvent(double yaw, double pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public double getYaw() {
        return yaw;
    }

    public double getPitch() {
        return pitch;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }
}
