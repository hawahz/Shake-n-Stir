package io.github.hawah.shakenstir.client.hanlder;

import io.github.hawah.shakenstir.client.ClickInteractions;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.item.ShakeItem;
import io.github.hawah.shakenstir.lib.client.handler.IHandler;
import io.github.hawah.shakenstir.lib.client.render.EaseHelper;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import io.github.hawah.shakenstir.util.Result;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector2d;

public class ShakeHandler implements IHandler {

    private boolean wasActive = false;
    private int lastSuccessTick = -1;
    private int shakeSuccessTimes = 0;
    private int firstShakeTick = -1;

    public void setX(double x) {
        this.x = Mth.lerp(0.2, this.x, Mth.clamp(x, -1, 2));
    }

    public void setY(double y) {
        this.y = Mth.lerp(0.1, this.y, Mth.clamp(y, -2, 2)) ;
    }

    private double x = 0, y = 0;

    private double oVx = 0, oVy = 0, vx = 0, vy = 0;

    public ShakeHandler() {
        ClickInteractions.registerMouseMove(this::onMouseMove);
    }

    @Override
    public void tick() {
        Vector2d vec = new Vector2d(x, y);
        Vector2d oVec = new Vector2d(oVx, oVy);
        assert Minecraft.getInstance().player != null;
        assert Minecraft.getInstance().level != null;
        double dot = vec.dot(oVec);
        if (dot < -0.125 && AnimationTickHolder.getTicks() - lastSuccessTick > 5) {
            float volumeWater = EaseHelper.easeInPow(Mth.clamp((float) shakeSuccessTimes / 50, 0, 0.8F), 6);
            Minecraft.getInstance().getSoundManager().play(
                    new SimpleSoundInstance(
                            SoundEvents.GLASS_HIT,
                            SoundSource.PLAYERS,
                            1.2F - volumeWater,
                            1,
                            Minecraft.getInstance().level.getRandom(),
                            Minecraft.getInstance().player.blockPosition()
                    )
            );
            Minecraft.getInstance().getSoundManager().play(
                    new SimpleSoundInstance(
                            SoundEvents.BUCKET_FILL,
                            SoundSource.PLAYERS,
                            volumeWater,
                            1,
                            Minecraft.getInstance().level.getRandom(),
                            Minecraft.getInstance().player.blockPosition()
                    )
            );
            lastSuccessTick = AnimationTickHolder.getTicks();
            shakeSuccessTimes ++;
        }
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    @Override
    public boolean isActive() {
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        return Minecraft.getInstance().mouseHandler.isRightPressed() &&
                player.getMainHandItem().getItem() instanceof ShakeItem &&
                ClientDataHolder.Picker.type().equals(HitResult.Type.MISS) &&
                !player.getCooldowns().isOnCooldown(player.getMainHandItem()) &&
                player.getMainHandItem().getOrDefault(DataComponentTypeRegistries.HAS_CUP, false);
    }

    public int firstTimeShake() {
        return firstShakeTick;
    }

    public void init() {
        shakeSuccessTimes = 0;
        firstShakeTick = AnimationTickHolder.getTicks();
        x = 0;
        y = 0;
        vx = 0;
        vy = 0;
    }
    public Result onMouseMove(final double yaw, final double pitch) {
        if (!isActive()) {
            wasActive = false;
            return Result.empty();
        }
        if (!wasActive) {
            firstShakeTick = AnimationTickHolder.getTicks();
            init();
        }
        oVx = vx;
        oVy = vy;

        double ox = x, oy = y;

        setX(x+yaw / 100);
        setY(y+pitch / 100);

        vx = x - ox;
        vy = y - oy;

        wasActive = true;
        tick();
        return new Result(isActive());
    }
}
