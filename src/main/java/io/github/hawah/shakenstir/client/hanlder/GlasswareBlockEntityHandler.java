package io.github.hawah.shakenstir.client.hanlder;

import io.github.hawah.shakenstir.client.ClickInteractions;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.content.block.Glassware;
import io.github.hawah.shakenstir.lib.client.KeyBinding;
import io.github.hawah.shakenstir.lib.client.handler.IHandler;
import io.github.hawah.shakenstir.util.Result;
import net.minecraft.client.Minecraft;

public class GlasswareBlockEntityHandler implements IHandler {
    private boolean wasActive = false;
    private double x = 0, y = 0;
    private double oVx = 0, oVy = 0, vx = 0, vy = 0;
    private double deltaX = 0, deltaY = 0;

    public GlasswareBlockEntityHandler() {
        ClickInteractions.registerMouseMove(this::onMouseMove);
    }

    public void init() {
        x = 0;
        y = 0;
        vx = 0;
        vy = 0;
    }

    @Override
    public void tick() {

    }

    @Override
    public boolean isActive() {
        if (PACKAGE.getPlayer() == null) {
            return false;
        }
        return PACKAGE.getPlayer().getMainHandItem().isEmpty() &&
                ClientDataHolder.Picker.block().isPresent() &&
                ClientDataHolder.Picker.block().get() instanceof Glassware &&
                KeyBinding.hasShiftDown() &&
                Minecraft.getInstance().mouseHandler.isRightPressed();
    }

    public Result onMouseMove(final double yaw, final double pitch) {
        if (isActive()) {
            deltaX = yaw / 100;
            deltaY = pitch / 100;
            tick();
            return new Result(true);
        }
        return Result.empty();
    }
}
