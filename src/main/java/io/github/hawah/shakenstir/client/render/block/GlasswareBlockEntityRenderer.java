package io.github.hawah.shakenstir.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.hawah.shakenstir.client.model.glassware.GlasswareQuadCollection;
import io.github.hawah.shakenstir.client.render.block.renderstate.GlasswareBlockEntityRenderState;
import io.github.hawah.shakenstir.client.render.glassware.vertexConsumer.VerticalGradientVertexConsumer;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.Ease;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3dc;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.util.List;

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
    }

    @Override
    public void submit(GlasswareBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(state.position.x, 0, state.position.y);
        poseStack.mulPose(Axis.YN.rotationDegrees(state.rotate));
        poseStack.translate(-0.5, 0, -0.5);
        VerticalGradientVertexConsumer vc = new VerticalGradientVertexConsumer();
        vc.setGradientStyle(Ease::outCirc);
        double bottom = state.model.getShape().min(Direction.Axis.Y);
        double top = state.model.getShape().max(Direction.Axis.Y);
        vc.setMinY((float) (bottom + (top - bottom) * 0.15));
        vc.setMaxY((float) (top - (top - bottom) * 0.1));
        vc.setSourceAlpha((int) (80 * state.height));
        vc.setTargetAlpha((int) (120 * state.height));
        vc.setModulate(state.color);
        state.model.submit(submitNodeCollector, poseStack, List.of(vc), state.lightCoords, OverlayTexture.NO_OVERLAY, RenderTypes.cutoutMovingBlock());
        if (state.height > 0 && state.model.getModel() instanceof GlasswareQuadCollection quadCollection) {
            Vector3dc start = quadCollection.start();
            Vector3dc end = quadCollection.end();

            float minX = (float) Math.min(start.x(), end.x());
            float minY = (float) Math.min(start.y(), end.y());
            float minZ = (float) Math.min(start.z(), end.z());

            float maxX = (float) Math.max(start.x(), end.x());
            float maxY = ((float) Math.max(start.y(), end.y()) - minY) * state.height + minY;
            float maxZ = (float) Math.max(start.z(), end.z());

            submitNodeCollector.submitCustomGeometry(
                    poseStack,
                    RenderTypes.debugQuads(),
                    (pose, buffer) -> {

                        Matrix4f mat = pose.pose();

                        int r = ARGB.red(state.color);
                        int g = ARGB.green(state.color);
                        int b = ARGB.blue(state.color);
                        int a = 80;

                        // DOWN
                        quad(buffer, mat,
                                minX, minY, minZ,
                                maxX, minY, minZ,
                                maxX, minY, maxZ,
                                minX, minY, maxZ,
                                r, g, b, a);

                        // UP
                        quad(buffer, mat,
                                minX, maxY, minZ,
                                minX, maxY, maxZ,
                                maxX, maxY, maxZ,
                                maxX, maxY, minZ,
                                r, g, b, a);

                        // NORTH
                        quad(buffer, mat,
                                minX, minY, minZ,
                                minX, maxY, minZ,
                                maxX, maxY, minZ,
                                maxX, minY, minZ,
                                r, g, b, a);

                        // SOUTH
                        quad(buffer, mat,
                                minX, minY, maxZ,
                                maxX, minY, maxZ,
                                maxX, maxY, maxZ,
                                minX, maxY, maxZ,
                                r, g, b, a);

                        // WEST
                        quad(buffer, mat,
                                minX, minY, minZ,
                                minX, minY, maxZ,
                                minX, maxY, maxZ,
                                minX, maxY, minZ,
                                r, g, b, a);

                        // EAST
                        quad(buffer, mat,
                                maxX, minY, minZ,
                                maxX, maxY, minZ,
                                maxX, maxY, maxZ,
                                maxX, minY, maxZ,
                                r, g, b, a);
                    }
            );
        }
        poseStack.popPose();
    }

    private static void quad(
            VertexConsumer buffer,
            Matrix4f mat,

            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,

            int r, int g, int b, int a
    ) {
        buffer.addVertex(mat, x1, y1, z1)
                .setColor(r, g, b, a);

        buffer.addVertex(mat, x2, y2, z2)
                .setColor(r, g, b, a);

        buffer.addVertex(mat, x3, y3, z3)
                .setColor(r, g, b, a);

        buffer.addVertex(mat, x4, y4, z4)
                .setColor(r, g, b, a);
    }

    @Override
    public GlasswareBlockEntityRenderState createRenderState() {
        return new GlasswareBlockEntityRenderState();
    }
}
