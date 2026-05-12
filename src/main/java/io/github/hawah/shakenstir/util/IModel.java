package io.github.hawah.shakenstir.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.util.ARGB;
import net.minecraft.util.Ease;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public interface IModel {
    StandaloneModelKey<QuadCollection> key();

    Models.ModelData<VoxelShape> voxelShape();

    default VoxelShape getShape() {
        if (voxelShape().mutable()) {
            QuadCollection qc = Minecraft.getInstance().getModelManager().getStandaloneModel(key());
            if (qc == null) {
                return Shapes.empty();
            }
            float minX = Float.MAX_VALUE;
            float minY = Float.MAX_VALUE;
            float minZ = Float.MAX_VALUE;
            float maxX = Float.MIN_VALUE;
            float maxY = Float.MIN_VALUE;
            float maxZ = Float.MIN_VALUE;
            for (BakedQuad quad : qc.getAll()) {
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

                    VertexConsumer warper = new VertexConsumer() {

                        @Override
                        public void putBakedQuad(PoseStack.Pose pose, BakedQuad quad, QuadInstance instance) {
                            Vector3fc normalVec = quad.direction().getUnitVec3f();
                            Matrix4f matrix = pose.pose();
                            Vector3f normal = pose.transformNormal(normalVec, new Vector3f());
                            int lightEmission = quad.materialInfo().lightEmission();

                            for (int vertex = 0; vertex < 4; vertex++) {
                                Vector3fc position = quad.position(vertex);
                                long packedUv = quad.packedUV(vertex);
                                int vertexColor = net.minecraft.util.ARGB.multiply(instance.getColor(vertex), quad.bakedColors().color(vertex)); // Neo: apply baked color from the quad
                                vertexColor = ARGB.alphaBlend(vertexColor, ARGB.color((int)(100 * Ease.outCubic(position.y())), 0xFF0000));
                                vertexColor = ARGB.color(ARGB.alpha(instance.getColor(vertex)), vertexColor);
                                int light = instance.getLightCoordsWithEmission(vertex, lightEmission);
                                Vector3f pos = matrix.transformPosition(position, new Vector3f());
                                float u = UVPair.unpackU(packedUv);
                                float v = UVPair.unpackV(packedUv);
                                applyBakedNormals(normal, quad.bakedNormals(), vertex, pose.normal()); // Neo: apply baked normals from the quad
                                this.addVertex(pos.x(), pos.y(), pos.z(), vertexColor, u, v, instance.overlayCoords(), light, normal.x(), normal.y(), normal.z());
                            }
                        }

                        @Override
                        public VertexConsumer addVertex(float x, float y, float z) {
                            buffer.addVertex(x, y, z);
                            return this;
                        }

                        @Override
                        public VertexConsumer setColor(int r, int g, int b, int a) {
                            buffer.setColor(r, g, b, a);
                            return this;
                        }

                        @Override
                        public VertexConsumer setColor(int color) {
                            buffer.setColor(color);
                            return this;
                        }

                        @Override
                        public VertexConsumer setUv(float u, float v) {
                            buffer.setUv(u, v);
                            return this;
                        }

                        @Override
                        public VertexConsumer setUv1(int u, int v) {
                            buffer.setUv1(u, v);
                            return this;
                        }

                        @Override
                        public VertexConsumer setUv2(int u, int v) {
                            buffer.setUv2(u, v);
                            return this;
                        }

                        @Override
                        public VertexConsumer setNormal(float x, float y, float z) {
                            buffer.setNormal(x, y, z);
                            return this;
                        }

                        @Override
                        public VertexConsumer setLineWidth(float width) {
                            buffer.setLineWidth(width);
                            return this;
                        }
                    };

                    gin.getAll().forEach(quad ->
                            warper.putBakedQuad(pose, quad, instance)
                    );
                }
        );
    }
}
