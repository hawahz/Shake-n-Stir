package io.github.hawah.shakenstir.lib.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;

public final class DebugGizmoRenderer {

    public static class Translation {
        public static void render(
                PoseStack poseStack,
                MultiBufferSource bufferSource,
                float length
        ) {
            VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.lines());

            PoseStack.Pose pose = poseStack.last();

            Matrix4f poseMatrix = pose.pose();

            // X - Red
            line(
                    consumer,
                    poseMatrix,
                    pose,
                    0, 0, 0,
                    length, 0, 0,
                    255, 0, 0, 255,
                    1, 0, 0
            );

            // Y - Green
            line(
                    consumer,
                    poseMatrix,
                    pose,
                    0, 0, 0,
                    0, length, 0,
                    0, 255, 0, 255,
                    0, 1, 0
            );

            // Z - Blue
            line(
                    consumer,
                    poseMatrix,
                    pose,
                    0, 0, 0,
                    0, 0, length,
                    0, 0, 255, 255,
                    0, 0, 1
            );
        }

        private static void line(
                VertexConsumer consumer,
                Matrix4f pose,
                PoseStack.Pose normal,

                float x1, float y1, float z1,
                float x2, float y2, float z2,

                int r, int g, int b, int a,

                float nx, float ny, float nz
        ) {
            consumer.addVertex(pose, x1, y1, z1)
                    .setColor(r, g, b, a)
                    .setNormal(normal, nx, ny, nz)
                    .setLineWidth(10.0f);

            consumer.addVertex(pose, x2, y2, z2)
                    .setColor(r, g, b, a)
                    .setNormal(normal, nx, ny, nz)
                    .setLineWidth(1.0f);
        }
    }

    public static final class RotationGizmoRenderer {

        public static void render(
                PoseStack poseStack,
                MultiBufferSource buffers,
                float radius
        ) {
            renderRing(
                    poseStack,
                    buffers,
                    Direction.Axis.X,
                    radius,
                    255, 0, 0
            );

            renderRing(
                    poseStack,
                    buffers,
                    Direction.Axis.Y,
                    radius,
                    0, 255, 0
            );

            renderRing(
                    poseStack,
                    buffers,
                    Direction.Axis.Z,
                    radius,
                    0, 0, 255
            );
        }

        private static void renderRing(
                PoseStack poseStack,
                MultiBufferSource buffers,

                Direction.Axis axis,

                float radius,

                int r,
                int g,
                int b
        ) {
            VertexConsumer consumer =
                    buffers.getBuffer(RenderTypes.lines());

            PoseStack.Pose pose = poseStack.last();

            Matrix4f mat = pose.pose();
            PoseStack.Pose normal = pose;

            int segments = 64;

            float prevX = 0;
            float prevY = 0;
            float prevZ = 0;

            for (int i = 0; i <= segments; i++) {

                float t =
                        (float)(Math.PI * 2.0 * i / segments);

                float x = 0;
                float y = 0;
                float z = 0;

                float cos = (float)Math.cos(t);
                float sin = (float)Math.sin(t);

                switch (axis) {

                    // rotX => YZ circle
                    case X -> {
                        y = cos * radius;
                        z = sin * radius;
                    }

                    // rotY => XZ circle
                    case Y -> {
                        x = cos * radius;
                        z = sin * radius;
                    }

                    // rotZ => XY circle
                    case Z -> {
                        x = cos * radius;
                        y = sin * radius;
                    }
                }

                if (i > 0) {

                    line(
                            consumer,
                            mat,
                            normal,

                            prevX, prevY, prevZ,
                            x, y, z,

                            r, g, b
                    );
                }

                prevX = x;
                prevY = y;
                prevZ = z;
            }

            renderArrow(
                    consumer,
                    mat,
                    normal,

                    axis,
                    radius,

                    r, g, b
            );
        }

        private static void line(
                VertexConsumer consumer,

                Matrix4f pose,
                PoseStack.Pose normal,

                float x1,
                float y1,
                float z1,

                float x2,
                float y2,
                float z2,

                int r,
                int g,
                int b
        ) {
            float nx = x2 - x1;
            float ny = y2 - y1;
            float nz = z2 - z1;

            float len =
                    (float)Math.sqrt(nx*nx + ny*ny + nz*nz);

            if (len > 0.0001f) {
                nx /= len;
                ny /= len;
                nz /= len;
            }

            consumer.addVertex(
                            pose,
                            x1, y1, z1
                    )
                    .setColor(r, g, b, 255)
                    .setNormal(normal, nx, ny, nz)
                    .setLineWidth(10.0f);

            consumer.addVertex(
                            pose,
                            x2, y2, z2
                    )
                    .setColor(r, g, b, 255)
                    .setNormal(normal, nx, ny, nz)
                    .setLineWidth(1.0f);
        }

        private static void renderArrow(
                VertexConsumer consumer,

                Matrix4f pose,
                PoseStack.Pose normal,

                Direction.Axis axis,

                float radius,

                int r,
                int g,
                int b
        ) {

            float t =
                    (float)(Math.PI / 4.0);

            float x = 0;
            float y = 0;
            float z = 0;

            float tx = 0;
            float ty = 0;
            float tz = 0;

            float cos = (float)Math.cos(t);
            float sin = (float)Math.sin(t);

            switch (axis) {

                case X -> {

                    y = cos * radius;
                    z = sin * radius;

                    // tangent
                    ty = -sin;
                    tz = cos;
                }

                case Y -> {

                    x = cos * radius;
                    z = sin * radius;

                    tx = -sin;
                    tz = cos;
                }

                case Z -> {

                    x = cos * radius;
                    y = sin * radius;

                    tx = -sin;
                    ty = cos;
                }
            }

            float arrowSize = radius * 0.15f;

            float ax1 = x - tx * arrowSize;
            float ay1 = y - ty * arrowSize;
            float az1 = z - tz * arrowSize;

            line(
                    consumer,
                    pose,
                    normal,

                    ax1, ay1, az1,
                    x, y, z,

                    r, g, b
            );
        }
    }
}
