package io.github.hawah.shakenstir.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.joml.Matrix4f;
import org.joml.Vector3dc;

import java.awt.*;

public class LiquidRenderer {

    public static Identifier DEFAULT = ShakenStir.asResource("textures/block/liquid_overlay.png");
    public static Identifier TEMP_TEXTURE = null;
    public static AnimateData animateData = null;

    public static void setTexture(Identifier texture) {
        TEMP_TEXTURE = texture;
    }
    public static void setAnimateData(AnimateData animateData) {
        LiquidRenderer.animateData = animateData;
    }
    public static void submitLiquid(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Vector3dc start, Vector3dc end, int lightCoords, int color) {
        submitLiquid(poseStack, submitNodeCollector, start, end, 1, lightCoords, color);
    }

    public static void submitLiquid(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Vector3dc start, Vector3dc end, float heightRate, int lightCoords, int color) {
        if (heightRate <= 1e-4) {
            return;
        }
        float minX = (float) Math.min(start.x(), end.x());
        float minY = (float) Math.min(start.y(), end.y());
        float minZ = (float) Math.min(start.z(), end.z());

        float maxX = (float) Math.max(start.x(), end.x());
        float maxY = ((float) Math.max(start.y(), end.y()) - minY) * heightRate + minY;
        float maxZ = (float) Math.max(start.z(), end.z());
        float u, v, width, height;
        if (animateData != null) {
            width = 1F / animateData.width();
            height = 1F / animateData.height();
            v = ((AnimationTickHolder.getTicks() % animateData.height())) * height;
            u =  (int) (AnimationTickHolder.getTicks() % animateData.width()) * width;
        } else {
            width = 1;
            height = 1;
            u = 0;
            v = 0;
        }

        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.entityTranslucent(TEMP_TEXTURE == null? DEFAULT: TEMP_TEXTURE),
                (pose, buffer) -> {

                    Matrix4f mat = pose.pose();

                    int r = ARGB.red(color);
                    int g = ARGB.green(color);
                    int b = ARGB.blue(color);
                    int a = 200;

                    // DOWN
                    quad(buffer, mat, pose,
                            minX, minY, minZ,
                            maxX, minY, minZ,
                            maxX, minY, maxZ,
                            minX, minY, maxZ,
                            r, g, b, a, lightCoords,
                            u, v, width, height);

                    // UP
                    quad(buffer, mat, pose,
                            minX, maxY, minZ,
                            minX, maxY, maxZ,
                            maxX, maxY, maxZ,
                            maxX, maxY, minZ,
                            r, g, b, a, lightCoords,
                            u, v, width, height);

                    // NORTH
                    quad(buffer, mat, pose,
                            minX, minY, minZ,
                            minX, maxY, minZ,
                            maxX, maxY, minZ,
                            maxX, minY, minZ,
                            r, g, b, a, lightCoords,
                            u, v, width, height);

                    // SOUTH
                    quad(buffer, mat, pose,
                            minX, minY, maxZ,
                            maxX, minY, maxZ,
                            maxX, maxY, maxZ,
                            minX, maxY, maxZ,
                            r, g, b, a, lightCoords,
                            u, v, width, height);

                    // WEST
                    quad(buffer, mat, pose,
                            minX, minY, minZ,
                            minX, minY, maxZ,
                            minX, maxY, maxZ,
                            minX, maxY, minZ,
                            r, g, b, a, lightCoords,
                            u, v, width, height);

                    // EAST
                    quad(buffer, mat, pose,
                            maxX, minY, minZ,
                            maxX, maxY, minZ,
                            maxX, maxY, maxZ,
                            maxX, minY, maxZ,
                            r, g, b, a, lightCoords,
                            u, v, width, height);
                }
        );
        TEMP_TEXTURE = null;
        animateData = null;
    }

    private static void quad(
            VertexConsumer buffer,
            Matrix4f mat,
            PoseStack.Pose pose,

            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,

            int r, int g, int b, int a,
            int lightCoords,

            float u, float v, float width, float height
    ) {
        float u2 = u + width;
        float v2 = v + height;

        buffer.addVertex(mat, x1, y1, z1)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(pose, 0f, 1f, 0f);

        buffer.addVertex(mat, x2, y2, z2)
                .setColor(r, g, b, a)
                .setUv(u, v2)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(pose, 0f, 1f, 0f);

        buffer.addVertex(mat, x3, y3, z3)
                .setColor(r, g, b, a)
                .setUv(u2, v2)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(pose, 0f, 1f, 0f);

        buffer.addVertex(mat, x4, y4, z4)
                .setColor(r, g, b, a)
                .setUv(u2, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(pose, 0f, 1f, 0f);
    }

    public record AnimateData(int width, int height){}
}
