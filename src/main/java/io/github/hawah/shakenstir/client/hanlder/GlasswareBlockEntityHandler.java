package io.github.hawah.shakenstir.client.hanlder;

import io.github.hawah.shakenstir.client.ClickInteractions;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.content.block.Glassware;
import io.github.hawah.shakenstir.content.item.GlasswareItem;
import io.github.hawah.shakenstir.lib.client.KeyBinding;
import io.github.hawah.shakenstir.lib.client.handler.IHandler;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import io.github.hawah.shakenstir.util.Result;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class GlasswareBlockEntityHandler implements IHandler {
    private boolean wasActive = false;
    private double x = 0, y = 0;
    private double oVx = 0, oVy = 0, vx = 0, vy = 0;

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
        if (getPlayer() == null) {
            return false;
        }
        return getPlayer().getMainHandItem().isEmpty() &&
                ClientDataHolder.Picker.block().isPresent() &&
                ClientDataHolder.Picker.block().get() instanceof Glassware &&
                KeyBinding.hasShiftDown() &&
                Minecraft.getInstance().mouseHandler.isMouseGrabbed();
    }

    public Result onMouseMove(final double yaw, final double pitch) {
        if (isActive()) {
            return new Result(true);
        }
        return new Result(false);
    }

    public static Player getPlayer() {
        return Minecraft.getInstance().player;
    }

    public static ItemStack getItem() {
        return getPlayer().getMainHandItem();
    }
}
