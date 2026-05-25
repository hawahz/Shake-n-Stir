package io.github.hawah.shakenstir.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.hawah.shakenstir.client.render.block.renderstate.GlasswareBlockEntityRenderState;
import io.github.hawah.shakenstir.client.render.general.GlasswareRenderer;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;


public class GlasswareBlockEntityRenderer implements BlockEntityRenderer<GlasswareBlockEntity, GlasswareBlockEntityRenderState> {
    public GlasswareBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void extractRenderState(GlasswareBlockEntity blockEntity, GlasswareBlockEntityRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.position.set(blockEntity.position);
        state.rotate = blockEntity.rotation;
        state.model = blockEntity.getModel();
        state.height = Mth.lerp(partialTicks, blockEntity.oHeight, blockEntity.height);
        state.color = blockEntity.getColor();
        state.decorations.addAll(blockEntity.decorationsList);
        state.lightCoords = LightCoordsUtil.pack(LightCoordsUtil.block(state.lightCoords) + 3, LightCoordsUtil.sky(state.lightCoords));
    }

    @Override
    public void submit(GlasswareBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.model() == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(state.position.x, 0, state.position.y);
        poseStack.mulPose(Axis.YN.rotationDegrees(state.rotate));
        poseStack.translate(-0.5, 0, -0.5);
        float offsetX = state.position.x();
        float offsetZ = state.position.y();
        int lightCoords = state.lightCoords;

        GlasswareRenderer.submitGlassware(state, poseStack, submitNodeCollector, lightCoords, offsetX, offsetZ, state.rotate, true);
        poseStack.popPose();
    }

    @Override
    public GlasswareBlockEntityRenderState createRenderState() {
        return new GlasswareBlockEntityRenderState();
    }
}
