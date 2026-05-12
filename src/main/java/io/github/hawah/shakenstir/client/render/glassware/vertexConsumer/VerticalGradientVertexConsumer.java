package io.github.hawah.shakenstir.client.render.glassware.vertexConsumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Function;

public class VerticalGradientVertexConsumer extends AbstractWarpedVC {
    public VerticalGradientVertexConsumer(VertexConsumer source) {
        super(source);
    }

    int modulate;
    int sourceAlpha;
    int targetAlpha;
    Function<Float, Float> gradientStyle = Float::floatValue;
    float minY = 0;
    float maxY = 1;

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
            vertexColor = ARGB.alphaBlend(vertexColor, ARGB.color((int) Mth.lerp(map(position.y()), sourceAlpha, targetAlpha), modulate & ~0xFF000000));
            int light = instance.getLightCoordsWithEmission(vertex, lightEmission);
            Vector3f pos = matrix.transformPosition(position, new Vector3f());
            float u = UVPair.unpackU(packedUv);
            float v = UVPair.unpackV(packedUv);
            applyBakedNormals(normal, quad.bakedNormals(), vertex, pose.normal()); // Neo: apply baked normals from the quad
            this.addVertex(pos.x(), pos.y(), pos.z(), vertexColor, u, v, instance.overlayCoords(), light, normal.x(), normal.y(), normal.z());
        }
    }

    private float extend(float raw) {
        return (raw - minY) / (maxY - minY);
    }

    private float map(float rawX) {
        return gradientStyle.apply(extend(rawX));
    }
}
