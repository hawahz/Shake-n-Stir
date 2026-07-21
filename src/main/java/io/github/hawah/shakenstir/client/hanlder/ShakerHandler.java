package io.github.hawah.shakenstir.client.hanlder;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.client.ClientSharedShakeParams;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.item.ShakerItem;
import io.github.hawah.shakenstir.foundation.event.EventHandler;
import io.github.hawah.shakenstir.foundation.event.RegisterEvent;
import io.github.hawah.shakenstir.foundation.event.client.LevelMouseMoveEvent;
import io.github.hawah.shakenstir.foundation.networking.ServerboundShakeFinishPacket;
import io.github.hawah.shakenstir.foundation.networking.ServerboundShakePramTransmitPacket;
import io.github.hawah.shakenstir.foundation.tags.SnsFluidTags;
import io.github.hawah.shakenstir.foundation.utils.ShakeUtil;
import io.github.hawah.shakenstir.lib.client.handler.IHandler;
import io.github.hawah.shakenstir.lib.client.render.EaseHelper;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import io.github.hawah.shakenstir.lib.networking.Networking;
import io.github.hawah.shakenstir.util.Result;
import io.github.hawah.shakenstir.util.Textures;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.joml.Vector2d;

import javax.annotation.ParametersAreNonnullByDefault;

import static io.github.hawah.shakenstir.client.hanlder.MC.*;

// TODO: 人工审查 | 2026-06-29 | Claude Code | 类型:注解迁移
// 概述: (1) 类上新增 @RegisterEvent 注解，使 Gradle generateEventSpi task 在编译期发现此类。
//        (2) onMouseMove 方法: @SnsEvent → @EventHandler (新版处理器注解)。
//        (3) import: SnsEvent → EventHandler + RegisterEvent。
// 涉及: 类声明新增 @RegisterEvent; onMouseMove() 注解从 @SnsEvent 改为 @EventHandler
// 原状: 无 @RegisterEvent 类注解; 使用 @SnsEvent 标记处理器方法;
//       import io.github.hawah.shakenstir.foundation.event.SnsEvent
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@RegisterEvent
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
        //ClickInteractions.registerMouseMove(this::onMouseMove);
    }

    public void successShake() {
        shakeSuccessTimes ++;
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
            float iceMeltProcess = getIceMeltProcess(currentTick);
            float volumeWater = shakeCubes == 0? 1.2F: EaseHelper.easeInPow(Mth.clamp(iceMeltProcess, 0, 0.8F), 6);

            if (shakeCubes != 0 || ShakeUtil.hasItem(getItem())){
                mc().getSoundManager().play(
                        new SimpleSoundInstance(
                                SoundEvents.ARMOR_EQUIP_IRON.value(),
                                SoundSource.PLAYERS,
                                1.2F - volumeWater,
                                20,
                                level().getRandom(),
                                getPlayer().blockPosition()
                        )
                );
                mc().getSoundManager().playDelayed(
                        new SimpleSoundInstance(
                                SoundEvents.GLASS_BREAK,
                                SoundSource.PLAYERS,
                                (1.2F - volumeWater) * 0.2F,
                                1.5F,
                                level().getRandom(),
                                getPlayer().blockPosition()
                        ),
                        1
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
            if (iceMeltProcess < 1 && shakeCubes > 0) {
                successShake();
            }
        }
    }

    private float getIceMeltProcess(int currentTick) {
        int iceCubes = getItem().getOrDefault(DataComponentTypeRegistries.SHAKE_ICE_CUBES, 0);
        int maxValidShakeTime = SHAKE_TICKS[Mth.clamp(iceCubes - 1, 0, SHAKE_TICKS.length - 1)];
        return (float) (currentTick - firstShakeTick) / maxValidShakeTime;
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
    
    @EventHandler
    public static void onMouseMove(LevelMouseMoveEvent event) {
        double pitch = event.getPitch();
        double yaw = event.getYaw();
        var result = ShakenStirClient.SHAKE_HANDLER.onMouseMove(yaw, pitch);
        if (result.cancelled()) {
            event.setCanceled(true);
        }
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
        int shakeCubes = getItem().getOrDefault(DataComponentTypeRegistries.SHAKE_ICE_CUBES, -1);
        if (shakeCubes < 0) {
            return 1;
        }
        int maxValidShakeTime = SHAKE_TICKS[Mth.clamp(shakeCubes - 1, 0, SHAKE_TICKS.length - 1)];
        return (float) Mth.clamp((float) (AnimationTickHolder.getTicks() - firstShakeTick) / maxValidShakeTime, 0F, 1);
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (!isActive()) {
            return;
        }
        double process = AnimationTickHolder.getRenderTime() - firstShakeTick;
        double fadeInProcess = Mth.clamp(process / 20, 0, 1);
        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        Font font = mc().font;
        String str = String.valueOf(shakeSuccessTimes);
        int x = (guiGraphics.guiWidth() - font.width(str)) / 2;
        int y = guiGraphics.guiHeight() - 24 - 9 - 2 - font.lineHeight;
        guiGraphics.text(font, str, x + 1, y, ARGB.color((int) (fadeInProcess * 255), 0xff000000), false);
        guiGraphics.text(font, str, x - 1, y, ARGB.color((int) (fadeInProcess * 255), 0xff000000), false);
        guiGraphics.text(font, str, x, y + 1, ARGB.color((int) (fadeInProcess * 255), 0xff000000), false);
        guiGraphics.text(font, str, x, y - 1, ARGB.color((int) (fadeInProcess * 255), 0xff000000), false);
        guiGraphics.text(font, str, x, y, ARGB.color((int) (fadeInProcess * 255), 0xff399a), false);

        float iceMeltProcess = getIceMeltProcess();

        Textures.SHAKER_PROGRESS.blit(
                guiGraphics,
                width * 7 / 8 - Textures.SHAKER_PROGRESS.getWidth(),
                height/2 - Textures.SHAKER_PROGRESS.getHeight()/2,
                ARGB.color((int) (fadeInProcess * 255), 0xffffff)
        );

        Textures.SHAKER_SLANTED.blit(
                guiGraphics,
                width * 7 / 8 - Textures.SHAKER_PROGRESS.getWidth() + 1,
                height/2 - Textures.SHAKER_PROGRESS.getHeight()/2 + (int)(iceMeltProcess * 4 * 16),
                ARGB.color((int) (fadeInProcess * 255), ARGB.linearLerp(0.8F, 0xFFFFFFFF, heatGradient(1-iceMeltProcess)))
        );


    }

    public static int heatGradient(float progress) {
        progress = Math.max(0f, Math.min(1f, progress));

        final int[] colors = {
                0xFF003F5B,
                0xFF1A5F63,
                0xFF517C65,
                0xFF8B9572,
                0xFFB1A372,
                0xFFC6A354,
                0xFFE0A136,
                0xFFFF9913
        };

        if (progress >= 1f) {
            return colors[colors.length - 1];
        }

        float position = progress * (colors.length - 1);
        int index = (int) position;
        float t = position - index;

        return lerpColor(colors[index], colors[index + 1], t);
    }

    private static int lerpColor(int c1, int c2, float t) {
        int a1 = (c1 >>> 24) & 0xFF;
        int r1 = (c1 >>> 16) & 0xFF;
        int g1 = (c1 >>> 8) & 0xFF;
        int b1 = c1 & 0xFF;

        int a2 = (c2 >>> 24) & 0xFF;
        int r2 = (c2 >>> 16) & 0xFF;
        int g2 = (c2 >>> 8) & 0xFF;
        int b2 = c2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public void finish() {
        ItemStack shaker = getItem();
        if (shakeSuccessTimes == 0 && ShakeUtil.getFluidStacks(shaker)
                .stream()
                .noneMatch(fluidStack -> fluidStack.is(SnsFluidTags.BUBBLE_LIKE))){
            return;
        }
        if (ShakeUtil.isEmpty(shaker)) {
            return;
        }
        assert getPlayer() != null;
        Networking.sendToServer(new ServerboundShakeFinishPacket(
                getPlayer().getUUID(),
                getItem(getPlayer()),
                shakeSuccessTimes,
                getIceMeltProcess(),
                shaker.getOrDefault(DataComponentTypeRegistries.SHAKE_ICE_CUBES, 0)
        ));
    }
}
