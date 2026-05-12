package io.github.hawah.shakenstir.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.hawah.shakenstir.client.render.block.renderstate.GlasswareBlockEntityRenderState;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import io.github.hawah.shakenstir.util.Models;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
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
    }

    @Override
    public void submit(GlasswareBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(state.position.x, 0, state.position.y);
        poseStack.mulPose(Axis.YN.rotationDegrees(state.rotate));
        poseStack.translate(-0.5, 0, -0.5);
        state.model.submit(submitNodeCollector, poseStack, state.lightCoords, OverlayTexture.NO_OVERLAY, RenderTypes.translucentMovingBlock());
        poseStack.popPose();
    }

    @Override
    public GlasswareBlockEntityRenderState createRenderState() {
        return new GlasswareBlockEntityRenderState();
    }
}
