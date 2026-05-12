package io.github.hawah.shakenstir.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.hawah.shakenstir.client.GlasswareRaycast;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.util.IModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import org.joml.Vector2f;

public record GlasswareOutlineRenderer(
        GlasswareBlockEntity blockEntity
) implements CustomBlockOutlineRenderer {

    @Override
    public boolean render(BlockOutlineRenderState renderState, MultiBufferSource.BufferSource buffer, PoseStack poseStack, boolean translucentPass, LevelRenderState levelRenderState) {
        Vector2f position = blockEntity.position;
        float rotation = blockEntity.rotation;
        IModel model = blockEntity.getModel();


        poseStack.pushPose();
        BlockPos pos = renderState.pos();
        Vec3 camPos = levelRenderState.cameraRenderState.pos;
        float camX = (float) camPos.x();
        float camY = (float) camPos.y();
        float camZ = (float) camPos.z();
        poseStack.translate(position.x(), 0, position.y());
        poseStack.translate(pos.getX() - camX, 0, pos.getZ() - camZ);
        poseStack.mulPose(Axis.YN.rotationDegrees(rotation));
        poseStack.translate(-(pos.getX() - camX), 0, -(pos.getZ() - camZ));

        GlasswareRaycast.shape = model.getShape();

        ShapeRenderer.renderShape(
                poseStack,
                buffer.getBuffer(RenderTypes.lines()),
                model.getShape(),
                pos.getX() - camX,
                pos.getY() - camY,
                pos.getZ() - camZ,
                renderState.highContrast() ? -11010079 : ARGB.black(102),
                Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth
        );
        buffer.endLastBatch();
        poseStack.popPose();
        return true;
    }
}
