package io.github.hawah.shakenstir.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.hawah.shakenstir.client.model.Models;
import io.github.hawah.shakenstir.client.render.glassware.vertexConsumer.AbstractWarpedVC;
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

import java.util.ArrayList;
import java.util.List;

public interface IModel<T> {
    StandaloneModelKey<T> key();

    Models.ModelData<VoxelShape> voxelShape();

    default VoxelShape getShape() {
        if (voxelShape().mutable()) {
            T qc = Minecraft.getInstance().getModelManager().getStandaloneModel(key());
            if (qc == null) {
                return Shapes.empty();
            }
            float minX = Float.MAX_VALUE;
            float minY = Float.MAX_VALUE;
            float minZ = Float.MAX_VALUE;
            float maxX = Float.MIN_VALUE;
            float maxY = Float.MIN_VALUE;
            float maxZ = Float.MIN_VALUE;
            QuadProvider provider = QuadProvider.parse(qc);
            for (BakedQuad quad : provider.getAll()) {
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
        submit(submitNodeCollector, poseStack, List.of(), lightCoords, overlayCoords);
    };
    default void submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, List<AbstractWarpedVC> warps, int lightCoords, int overlayCoords) {
        submit(submitNodeCollector, poseStack, warps, lightCoords, overlayCoords, RenderTypes.translucentMovingBlock());
    };
    default void submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, int lightCoords, int overlayCoords, RenderType renderType) {
        submit(submitNodeCollector, poseStack, List.of(), lightCoords, overlayCoords, renderType);
    };
    default void submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, List<AbstractWarpedVC> warps, int lightCoords, int overlayCoords, RenderType renderType) {
        submit(submitNodeCollector, poseStack, warps, lightCoords, overlayCoords, renderType, 0xFFFFFFFF);
    };
    default void submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, int lightCoords, int overlayCoords, RenderType renderType, int color) {
        submit(submitNodeCollector, poseStack, new ArrayList<>(), lightCoords, overlayCoords, renderType, color);
    }
    default void submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, List<AbstractWarpedVC> warps, int lightCoords, int overlayCoords, RenderType renderType, int color) {
        T model = Minecraft.getInstance().getModelManager().getStandaloneModel(key());
        if (model == null) {
            return;
        }
        QuadProvider provider = QuadProvider.parse(model);
        submitNodeCollector.submitCustomGeometry(
                poseStack,
                renderType,
                (pose, buffer) -> {
                    QuadInstance instance = new QuadInstance();
                    instance.setLightCoords(lightCoords);
                    instance.setOverlayCoords(overlayCoords);
                    instance.setColor(color);

                    VertexConsumer vc = buffer;

                    for (AbstractWarpedVC abstractWarpedVC : warps) {
                        vc = abstractWarpedVC.warp(vc);
                    }

                    final VertexConsumer finalVc = vc;
                    provider.getAll().forEach(quad ->
                            finalVc.putBakedQuad(pose, quad, instance)
                    );
                }
        );
    }

    default T getModel() {
        return Minecraft.getInstance().getModelManager().getStandaloneModel(key());
    }

    interface QuadProvider {
        List<BakedQuad> getAll();
        static QuadProvider parse(Object obj) {
            if (obj instanceof QuadCollection collection) {
                return new WarpedQuadCollection(collection);
            } else if (obj instanceof QuadProvider provider) {
                return provider;
            }
            throw new IllegalArgumentException("Invalid argument type: " + obj.getClass().getName());
        }
    }

    record WarpedQuadCollection(QuadCollection collection) implements QuadProvider {
        @Override
        public List<BakedQuad> getAll() {
            return collection.getAll();
        }
    }
}
