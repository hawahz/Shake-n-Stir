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
    public VerticalGradientVertexConsumer() {
        super();
    }
    public static VerticalGradientVertexConsumer INSTANCE = new VerticalGradientVertexConsumer();

    int modulate;
    int sourceAlpha = 0;
    int targetAlpha = 255;
    Function<Float, Float> gradientStyle = Float::floatValue;
    float minY = 0;
    float maxY = 1;

    public void setModulate(int modulate) {
        this.modulate = modulate;
    }

    public void setSourceAlpha(int sourceAlpha) {
        this.sourceAlpha = sourceAlpha;
    }

    public void setTargetAlpha(int targetAlpha) {
        this.targetAlpha = targetAlpha;
    }

    public void setGradientStyle(Function<Float, Float> gradientStyle) {
        this.gradientStyle = gradientStyle;
    }

    public void setMinY(float minY) {
        this.minY = minY;
    }

    public void setMaxY(float maxY) {
        this.maxY = maxY;
    }

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
            int alpha = (int) Mth.lerp(map(position.y()), sourceAlpha, targetAlpha);
            float t = Math.min(1, alpha / 255.0f);

            if (alpha > sourceAlpha){
                int sr = (modulate >> 16) & 0xFF;
                int sg = (modulate >> 8) & 0xFF;
                int sb = modulate & 0xFF;

                int dr = (vertexColor >> 16) & 0xFF;
                int dg = (vertexColor >> 8) & 0xFF;
                int db = vertexColor & 0xFF;

                float[] srcHSV = java.awt.Color.RGBtoHSB(sr, sg, sb, null);
                float[] dstHSV = java.awt.Color.RGBtoHSB(dr, dg, db, null);

// 保留原亮度
                float h = srcHSV[0];
                float s = Mth.lerp(t, dstHSV[1], srcHSV[1]);
                float cv = dstHSV[2];

                int rgb = java.awt.Color.HSBtoRGB(h, s, cv);

                vertexColor =
                        (vertexColor & 0xFF000000) |
                                (rgb & 0x00FFFFFF);
            }
            int light = instance.getLightCoordsWithEmission(vertex, lightEmission);
            Vector3f pos = matrix.transformPosition(position, new Vector3f());
            float u = UVPair.unpackU(packedUv);
            float v = UVPair.unpackV(packedUv);
            applyBakedNormals(normal, quad.bakedNormals(), vertex, pose.normal()); // Neo: apply baked normals from the quad
            this.addVertex(pos.x(), pos.y(), pos.z(), vertexColor, u, v, instance.overlayCoords(), light, normal.x(), normal.y(), normal.z());
        }
    }

    private static int screen(int base, int blend) {
        return 255 - ((255 - base) * (255 - blend) / 255);
    }

    private float extend(float raw) {
        return (raw - minY) / (maxY - minY);
    }

    private float map(float rawX) {
        return gradientStyle.apply(extend(rawX));
    }
}
