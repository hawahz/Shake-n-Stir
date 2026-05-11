package io.github.hawah.shakenstir.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.joml.Vector3f;

public interface IModel {
    StandaloneModelKey<QuadCollection> key();

    Models.ModelData<VoxelShape> voxelShape();

    default VoxelShape getShape() {
        if (voxelShape().mutable()) {
            QuadCollection gin = Minecraft.getInstance().getModelManager().getStandaloneModel(key());
            float minX = Float.MAX_VALUE;
            float minY = Float.MAX_VALUE;
            float minZ = Float.MAX_VALUE;
            float maxX = Float.MIN_VALUE;
            float maxY = Float.MIN_VALUE;
            float maxZ = Float.MIN_VALUE;
            for (BakedQuad quad : gin.getAll()) {
                for (int i = 0; i < 4; i++) {
                    Vector3f pos = (Vector3f) quad.position(i);
                    minX = Math.min(minX, pos.x());
                    minY = Math.min(minY, pos.y());
                    minZ = Math.min(minZ, pos.z());
                    maxX = Math.max(maxX, pos.x());
                    maxY = Math.max(maxY, pos.y());
                    maxZ = Math.max(maxZ, pos.z());
                }
            }

            voxelShape().setValue(
                    Shapes.box(
                            (minX - maxX)/2, minY, (minZ - maxZ)/2,
                            (maxX - minX)/2, maxY, (maxZ - minZ)/2
                    )
            );
        }
        return voxelShape().get();
    }

    default void submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, int lightCoords, int overlayCoords) {
        submit(submitNodeCollector, poseStack, lightCoords, overlayCoords, RenderTypes.translucentMovingBlock());
    };
    default void submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, int lightCoords, int overlayCoords, RenderType renderType) {
        submit(submitNodeCollector, poseStack, lightCoords, overlayCoords, renderType, 0xFFFFFFFF);
    };
    default void submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, int lightCoords, int overlayCoords, RenderType renderType, int color) {
        QuadCollection gin = Minecraft.getInstance().getModelManager().getStandaloneModel(key());
        if (gin == null) {
            return;
        }
        submitNodeCollector.submitCustomGeometry(
                poseStack,
                renderType,
                (pose, buffer) -> {
                    QuadInstance instance = new QuadInstance();
                    instance.setLightCoords(lightCoords);
                    instance.setOverlayCoords(overlayCoords);
                    instance.setColor(color);
                    gin.getAll().forEach(quad ->
                            buffer.putBakedQuad(pose, quad, instance)
                    );
                }
        );
    }
}
