package io.github.hawah.shakenstir.client.hanlder;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.client.ClickInteractions;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.client.ClientSharedShakeParams;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.item.ShakerItem;
import io.github.hawah.shakenstir.foundation.networking.ServerboundShakeFinishPacket;
import io.github.hawah.shakenstir.foundation.networking.ServerboundShakePramTransmitPacket;
import io.github.hawah.shakenstir.foundation.utils.ShakeUtil;
import io.github.hawah.shakenstir.lib.client.handler.IHandler;
import io.github.hawah.shakenstir.lib.client.render.EaseHelper;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import io.github.hawah.shakenstir.lib.networking.Networking;
import io.github.hawah.shakenstir.util.Result;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.joml.Vector2d;

import javax.annotation.ParametersAreNonnullByDefault;

import static io.github.hawah.shakenstir.client.hanlder.PACKAGE.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ShakerHandler implements IHandler, GuiLayer {

    private boolean wasActive = false;
    private int lastSuccessTick = -1;
    private int shakeSuccessTimes = 0;
    private int firstShakeTick = -1;
    private final static int[] SHAKE_TICKS = new int[] {200, 300, 360};
    private ItemStack item = null;
    private double lastSentX, lastSentY;
    private double lastSentVx, lastSentVy;
    private int lastSendTick = -999;
    private static final double SEND_THRESHOLD = 0.015;
    private static final int MAX_SEND_INTERVAL = 3;

    public void setX(double x) {
        this.x = Mth.lerp(0.2, this.x, Mth.clamp(x, -1, 2));
    }

    public void setY(double y) {
        this.y = Mth.lerp(0.1, this.y, Mth.clamp(y, -2, 2)) ;
    }

    private double x = 0, y = 0;

    private double oVx = 0, oVy = 0, vx = 0, vy = 0;

    public ShakerHandler() {
        ClickInteractions.registerMouseMove(this::onMouseMove);
    }

    @Override
    public void tick() {
        if (!isActive()) {
            return;
        }
        int currentTick = AnimationTickHolder.getTicks();
        double dx = Math.abs(x - lastSentX);
        double dy = Math.abs(y - lastSentY);
        boolean dirChanged = (vx * lastSentVx < 0) || (vy * lastSentVy < 0);
        boolean significantMove = dx > SEND_THRESHOLD || dy > SEND_THRESHOLD;
        boolean overdue = (currentTick - lastSendTick) >= MAX_SEND_INTERVAL;

        if (significantMove || dirChanged || overdue) {
            Networking.sendToServer(new ServerboundShakePramTransmitPacket(x, y, getPlayer().getId()));
            lastSentX = x;
            lastSentY = y;
            lastSentVx = vx;
            lastSentVy = vy;
            lastSendTick = currentTick;
        }
    }
    public void update() {
        Vector2d vec = new Vector2d(x, y);
        Vector2d oVec = new Vector2d(oVx, oVy);
        assert getPlayer() != null;
        assert level() != null;
        double dot = vec.dot(oVec);
        ClientSharedShakeParams.updateParam(
                getPlayer().getId(),
                x,
                y
        );
        int currentTick = AnimationTickHolder.getTicks();
        if (dot < -0.125 && currentTick - lastSuccessTick > 1) {
            int shakeCubes = getItem().getOrDefault(DataComponentTypeRegistries.SHAKE_ICE_CUBES, 0);
            int maxValidShakeTime = SHAKE_TICKS[Mth.clamp(shakeCubes - 1, 0, SHAKE_TICKS.length - 1)];
            float iceMeltProcess = (float) (currentTick - firstShakeTick) / maxValidShakeTime;
            float volumeWater = shakeCubes == 0? 1.2F: EaseHelper.easeInPow(Mth.clamp(iceMeltProcess, 0, 0.8F), 6);

            if (shakeCubes != 0 || ShakeUtil.hasItem(getItem())){
                mc().getSoundManager().play(
                        new SimpleSoundInstance(
                                SoundEvents.GLASS_HIT,
                                SoundSource.PLAYERS,
                                1.2F - volumeWater,
                                1,
                                level().getRandom(),
                                getPlayer().blockPosition()
                        )
                );
            }
            if (ShakeUtil.hasFluid(getItem())) {
                mc().getSoundManager().play(
                        new SimpleSoundInstance(
                                SoundEvents.BUCKET_FILL,
                                SoundSource.PLAYERS,
                                volumeWater,
                                1,
                                level().getRandom(),
                                getPlayer().blockPosition()
                        )
                );
            }
            lastSuccessTick = currentTick;
            if (iceMeltProcess < 1) {
                shakeSuccessTimes ++;
            }
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
        LocalPlayer player = getPlayer();
        if (player == null) {
            return false;
        }
        return player.isUsingItem() &&
                getItem(player).getItem() instanceof ShakerItem &&
                ClientDataHolder.Picker.type().equals(HitResult.Type.MISS) &&
                !player.getCooldowns().isOnCooldown(getItem(player)) &&
                getItem(player).getOrDefault(DataComponentTypeRegistries.HAS_CUP, false);
    }

    public void end() {
        item = null;
    }

    private ItemStack getItem(LocalPlayer player) {
        if (item != null) {
            return item;
        }
        return item = player.getMainHandItem();
    }

    private ItemStack getItem() {
        if (item != null) {
            return item;
        }
        return getItem(getPlayer());
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
        lastSentX = 0;
        lastSentY = 0;
        lastSentVx = 0;
        lastSentVy = 0;
        lastSendTick = AnimationTickHolder.getTicks();
    }
    public Result onMouseMove(final double yaw, final double pitch) {
        if (!isActive()) {
            if (wasActive) {
                finish();
            }
            wasActive = false;
            end();
            return Result.empty();
        }
        if (!wasActive) {
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
        update();
        end();
        return new Result(isActive());
    }

    public float getIceMeltProcess() {
        int shakeCubes = getItem().getOrDefault(DataComponentTypeRegistries.SHAKE_ICE_CUBES, 0);
        int maxValidShakeTime = SHAKE_TICKS[Mth.clamp(shakeCubes - 1, 0, SHAKE_TICKS.length - 1)];
        return (float) (AnimationTickHolder.getTicks() - firstShakeTick) / maxValidShakeTime;
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (!isActive()) {
            return;
        }
        double process = AnimationTickHolder.getRenderTime() - firstShakeTick;
        double fadeInProcess = Mth.clamp(process / 20, 0, 1);
    }

    public void finish() {
        if (shakeSuccessTimes == 0) {
            return;
        }
        assert getPlayer() != null;
        Networking.sendToServer(new ServerboundShakeFinishPacket(
                getPlayer().getUUID(),
                getItem(getPlayer()),
                shakeSuccessTimes,
                getIceMeltProcess(),
                getItem().getOrDefault(DataComponentTypeRegistries.SHAKE_ICE_CUBES, 0)
        ));
    }
}
