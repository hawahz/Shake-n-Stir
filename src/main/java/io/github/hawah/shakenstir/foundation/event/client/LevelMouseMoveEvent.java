package io.github.hawah.shakenstir.foundation.event.client;

import io.github.hawah.shakenstir.foundation.event.AbstractSnsEvent;
import io.github.hawah.shakenstir.foundation.event.Side;
import io.github.hawah.shakenstir.foundation.event.SnsRegisterEvent;

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
