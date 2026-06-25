package io.github.hawah.shakenstir.client.hanlder;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;

import static io.github.hawah.shakenstir.client.hanlder.MC.*;

public class MissStepHandler extends ActiveTriggerHandler {

    boolean wasActive = false;

    @Override
    public void onTick() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public boolean isActive() {
        return getPlayer() != null && getPlayer().hasEffect(MobEffectRegistries.MISS_STEP);
    }

    public void extract(ExtractLevelRenderStateEvent event) {
        if (!isPresent()) {
            return;
        }
        LevelRenderState renderState = event.getRenderState();
        CameraRenderState cameraRenderState = renderState.cameraRenderState;
        int r = cameraRenderState.xRot>0? (int) (cameraRenderState.xRot / 90F* 255) : 0;
        renderState.setRenderData(MISS_STEP, new MissStepRenderState(true, r));
    }

    public void submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, LevelRenderState levelRenderState) {
        var renderData = levelRenderState.getRenderData(MISS_STEP);
        if (renderData == null || !renderData.present()) {
            return;
        }
        CameraRenderState cameraRenderState = levelRenderState.cameraRenderState;
        int r = renderData.rChannel;
        int sign = ARGB.color(r, 0, 0);
        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.debugQuads(),
                (pose, buffer) -> {
                    PoseStack stack = new PoseStack();
                    stack.last().set(pose);
                    unbobView(cameraRenderState, stack);
                    stack.mulPose(Axis.YN.rotationDegrees(cameraRenderState.yRot));
                    stack.mulPose(Axis.XP.rotationDegrees(cameraRenderState.xRot));
                    stack.translate(0, 0, 0.5F);
                    PoseStack.Pose p = stack.last();
                    Window window = Minecraft.getInstance().getWindow();
                    float size = 1.5F/Math.min(window.getWidth(), window.getHeight());
                    buffer.addVertex(p, -size, -size, 0)
                            .setColor(sign);
                    buffer.addVertex(p, size, -size, 0)
                            .setColor(sign);
                    buffer.addVertex(p, size, size, 0)
                            .setColor(sign);
                    buffer.addVertex(p, -size, size, 0)
                            .setColor(sign);
                }
        );
        poseStack.popPose();
    }

    private static void unbobView(CameraRenderState cameraState, PoseStack poseStack) {
        if (cameraState.entityRenderState.isPlayer) {
            float backwardsInterpolatedWalkDistance = cameraState.entityRenderState.backwardsInterpolatedWalkDistance;
            float bob = cameraState.entityRenderState.bob;
            poseStack.mulPose(Axis.YN.rotationDegrees(180 + cameraState.yRot));
            poseStack.mulPose(Axis.XP.rotationDegrees(-cameraState.xRot));

            poseStack.mulPose(Axis.XP.rotationDegrees(-Math.abs(Mth.cos(backwardsInterpolatedWalkDistance * (float) Math.PI - 0.2F) * bob) * 5.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-Mth.sin(backwardsInterpolatedWalkDistance * (float) Math.PI) * bob * 3.0F));
            poseStack.translate(
                    -Mth.sin(backwardsInterpolatedWalkDistance * (float) Math.PI) * bob * 0.5F,
                    Math.abs(Mth.cos(backwardsInterpolatedWalkDistance * (float) Math.PI) * bob),
                    0.0F
            );

            poseStack.mulPose(Axis.XP.rotationDegrees(cameraState.xRot));
            poseStack.mulPose(Axis.YN.rotationDegrees(-180 - cameraState.yRot));
        }
    }

    public static final ContextKey<MissStepRenderState> MISS_STEP = ShakenStir.asContextKey("miss_step");

    private record MissStepRenderState(boolean present, int rChannel) {

    }
}
