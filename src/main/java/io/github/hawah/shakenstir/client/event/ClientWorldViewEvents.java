package io.github.hawah.shakenstir.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.client.render.GlasswareOutlineRenderer;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.lib.client.render.outliner.Outliner;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

import java.lang.ref.WeakReference;

import static io.github.hawah.shakenstir.client.event.MC.getLevel;
import static io.github.hawah.shakenstir.client.event.MC.getPlayer;

@EventBusSubscriber(Dist.CLIENT)
public class ClientWorldViewEvents {
    public static final float FOG_LERP = 0.01F;
    private static double cameraRoll = 0;
    private static double shakeIntensity = 0;
    private static WeakReference<ClientLevel> previousLevel = new WeakReference<>(null);
    private static float cr = -1;
    private static float cg = -1;
    private static float cb = -1;

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent.AfterLevel event) {
        ShakenStirClient.TIMER_NORMAL.warp(Minecraft.getInstance().getDeltaTracker());
    }

    @SubscribeEvent
    public static void onExtractLevelStage(ExtractLevelRenderStateEvent event) {

        ShakenStirClient.GLASSWARE_HANDLER.extract(event);
        ShakenStirClient.DECORATE_PLACE_HANDLER.extract(event);
        Outliner.extract(event);
    }

    @SubscribeEvent
    public static void onSubmitLevel(SubmitCustomGeometryEvent event) {
        SubmitNodeCollector submitNodeCollector = event.getSubmitNodeCollector();
        PoseStack poseStack = event.getPoseStack();
        LevelRenderState levelRenderState = event.getLevelRenderState();

        poseStack.pushPose();

        ShakenStirClient.GLASSWARE_HANDLER.submit(submitNodeCollector, poseStack, levelRenderState);
        ShakenStirClient.DECORATE_PLACE_HANDLER.submit(submitNodeCollector, poseStack, levelRenderState);
        Outliner.submit(submitNodeCollector, poseStack, levelRenderState);
        poseStack.popPose();

    }



    @SubscribeEvent
    public static void modifyFov(ComputeFovModifierEvent event) {
        float modifier = event.getFovModifier();
        if (ClientDataHolder.shouldModifyView()) {
            modifier /= 2;

        }
        Player player = event.getPlayer();
        if (player.isUsingItem()) {
            if (player.getUseItem().is(ItemRegistries.SQUEEZER) && player.getUseItem().has(DataComponentTypeRegistries.SQUEEZER_HOLDER)) {
                float scale = Math.min((float) player.getTicksUsingItem() / player.getUseItemRemainingTicks(), 2.0F);
                modifier *= 1.0F - scale * 0.15F;
            }
        }

        event.setNewFovModifier(modifier);
    }

    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        // TODO 取消副手手套渲染
    }

    @SubscribeEvent
    public static void modifyCameraRoll(ViewportEvent.ComputeCameraAngles event) {
        if (getPlayer() == null) {
            return;
        }
        if (!getPlayer().hasEffect(MobEffectRegistries.DRUNK) && !(cameraRoll > 0 && shakeIntensity > 0)) {
            return;
        }
        DeltaTracker deltaTracker = Minecraft.getInstance().getDeltaTracker();
        float deltaTicks = deltaTracker.getGameTimeDeltaTicks();
        int amplifier = getPlayer().hasEffect(MobEffectRegistries.DRUNK) ? getPlayer().getEffect(MobEffectRegistries.DRUNK).getAmplifier() : 0;
        float renderTime = AnimationTickHolder.getRenderTime();
        cameraRoll = Mth.lerp(0.01 * deltaTicks / 0.68, cameraRoll, amplifier / 3F);
        shakeIntensity = Mth.lerp(0.01 * deltaTicks / 0.68, cameraRoll, Math.max(0, amplifier - 5));
        event.setRoll(event.getRoll() + (float) (Math.sin(renderTime /20) * cameraRoll));
        event.setPitch((float) ((event.getPitch() + Math.sin(renderTime /20D) * shakeIntensity)));
        event.setYaw((float) ((event.getYaw() + Math.cos(renderTime /20D) * shakeIntensity)));
    }

    @SubscribeEvent
    public static void modifyFogColor(ViewportEvent.ComputeFogColor event) {
        if (getPlayer() == null) {
            return;
        }
        float r;
        float g;
        float b;

        float deltaTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
        float lerp = (float) (FOG_LERP * deltaTicks / 0.68);

        if ((cr < 0 || cg < 0 || cb < 0) || !previousLevel.refersTo(getLevel())) {
            cr = event.getRed();
            cg = event.getGreen();
            cb = event.getBlue();
            previousLevel.clear();
            previousLevel = new WeakReference<>(getLevel());
        }

        MobEffectInstance drunkEffect = getPlayer().getEffect(MobEffectRegistries.DRUNK);
        if (drunkEffect != null && drunkEffect.getAmplifier() >= 3) {
            r = 255 / 255F;
            g = 109/255F;
            b = 120/255F;

            if (drunkEffect.endsWithin(200)) {


                r = event.getRed();
                g = event.getGreen();
                b = event.getBlue();
            }
        } else {
            r = event.getRed();
            g = event.getGreen();
            b = event.getBlue();
        }
        if (r != cr || g != cg || b != cb) {
            cr = Mth.lerp(lerp, cr, r);
            cg = Mth.lerp(lerp, cg, g);
            cb = Mth.lerp(lerp, cb, b);
        }
        if (drunkEffect != null) {
            event.setRed(cr);
            event.setGreen(cg);
            event.setBlue(cb);
        }
    }

    @SubscribeEvent
    public static void onRenderOutline(ExtractBlockOutlineRenderStateEvent event) {
        if (event.getLevel().getBlockEntity(event.getBlockPos()) instanceof GlasswareBlockEntity blockEntity) {
            event.addCustomRenderer(new GlasswareOutlineRenderer(blockEntity));
        }
    }
}
