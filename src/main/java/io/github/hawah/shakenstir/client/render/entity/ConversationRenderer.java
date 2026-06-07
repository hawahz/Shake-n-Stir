package io.github.hawah.shakenstir.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;

import java.util.function.BiConsumer;

record ConversationRenderer(
        BartenderRenderState state,
        Font font,
        String text
) implements SubmitNodeCollector.CustomGeometryRenderer {

    @Override
    public void render(PoseStack.Pose pose, VertexConsumer buffer) {

        final int textWidth = font.width(text);
        final int textHeight = font.lineHeight;

        // ===== 可调参数 =====
        final float atlasGrid = 4.0F;
        final float uvStep = 1.0F / atlasGrid;

        // 图集块缩放
        final float atlasScale = 0.5F;

        // 箭头缩放
        final float arrowScale = 1.0F;

        // 文本留白
        final float paddingX = 16F;
        final float paddingY = 8F;

        // 气泡主体尺寸
        final float bubbleWidth = textWidth + paddingX * 2.0F;
        final float bubbleHeight = textHeight + paddingY * 2.0F;

        // 单个角块尺寸
        final float tilePixelSize = 16.0F * atlasScale;

        final float cornerSize = Math.clamp(Math.min(bubbleWidth, bubbleHeight) * 0.5F - 1.0F, 1.0F, tilePixelSize);

        // 关键：这里是“上正下负”，和 y 翻转后的 poseStack 对齐
        final float left = -bubbleWidth / 2.0F;
        final float right = bubbleWidth / 2.0F;
        final float top = textHeight + paddingY;
        final float bottom = -paddingY;

        BiConsumer<float[], float[]> drawQuad = (pos, uv) -> {
            float x0 = pos[0];
            float y0 = pos[1];
            float x1 = pos[2];
            float y1 = pos[3];

            float u0 = uv[0];
            float v0 = uv[1];
            float u1 = uv[2];
            float v1 = uv[3];

            buffer.addVertex(pose, x0, y1, 0)
                    .setColor(-1)
                    .setUv(u0, v1)
                    .setLight(state.lightCoords);

            buffer.addVertex(pose, x1, y1, 0)
                    .setColor(-1)
                    .setUv(u1, v1)
                    .setLight(state.lightCoords);

            buffer.addVertex(pose, x1, y0, 0)
                    .setColor(-1)
                    .setUv(u1, v0)
                    .setLight(state.lightCoords);

            buffer.addVertex(pose, x0, y0, 0)
                    .setColor(-1)
                    .setUv(u0, v0)
                    .setLight(state.lightCoords);
        };

        // 1 左上角
        drawQuad.accept(
                new float[]{
                        left,
                        bottom,
                        left + cornerSize,
                        bottom + cornerSize
                },
                new float[]{
                        0.0F * uvStep, 0.0F * uvStep,
                        1.0F * uvStep, 1.0F * uvStep
                }
        );

        // 2 上边
        drawQuad.accept(
                new float[]{
                        left + cornerSize,
                        bottom,
                        right - cornerSize,
                        bottom + cornerSize
                },
                new float[]{
                        1.0F * uvStep, 0.0F * uvStep,
                        2.0F * uvStep, 1.0F * uvStep
                }
        );

        // 3 右上角
        drawQuad.accept(
                new float[]{
                        right - cornerSize,
                        bottom,
                        right,
                        bottom + cornerSize
                },
                new float[]{
                        2.0F * uvStep, 0.0F * uvStep,
                        3.0F * uvStep, 1.0F * uvStep
                }
        );

        // 4 左边
        drawQuad.accept(
                new float[]{
                        left,
                        bottom + cornerSize,
                        left + cornerSize,
                        top - cornerSize
                },
                new float[]{
                        0.0F * uvStep, 1.0F * uvStep,
                        1.0F * uvStep, 2.0F * uvStep
                }
        );

        // 5 中间
        drawQuad.accept(
                new float[]{
                        left + cornerSize,
                        bottom + cornerSize,
                        right - cornerSize,
                        top - cornerSize
                },
                new float[]{
                        1.0F * uvStep, 1.0F * uvStep,
                        2.0F * uvStep, 2.0F * uvStep
                }
        );

        // 6 右边
        drawQuad.accept(
                new float[]{
                        right - cornerSize,
                        bottom + cornerSize,
                        right,
                        top - cornerSize
                },
                new float[]{
                        2.0F * uvStep, 1.0F * uvStep,
                        3.0F * uvStep, 2.0F * uvStep
                }
        );

        // 9 左下角
        drawQuad.accept(
                new float[]{
                        left,
                        top - cornerSize,
                        left + cornerSize,
                        top
                },
                new float[]{
                        0.0F * uvStep, 2.0F * uvStep,
                        1.0F * uvStep, 3.0F * uvStep
                }
        );

        // 8 下边
        drawQuad.accept(
                new float[]{
                        left + cornerSize,
                        top - cornerSize,
                        right - cornerSize,
                        top
                },
                new float[]{
                        1.0F * uvStep, 2.0F * uvStep,
                        2.0F * uvStep, 3.0F * uvStep
                }
        );

        // 11 右下角
        drawQuad.accept(
                new float[]{
                        right - cornerSize,
                        top - cornerSize,
                        right,
                        top
                },

                new float[]{
                        2.0F * uvStep, 2.0F * uvStep,
                        3.0F * uvStep, 3.0F * uvStep
                }
        );

        // 12 底部箭头：放在气泡底边正中间下面
        float arrowWidth = 16.0F * atlasScale * arrowScale;
        float arrowHeight = 16.0F * atlasScale * arrowScale;

        pose.translate(0, 0, 0.001F);
        drawQuad.accept(
                new float[]{
                        -arrowWidth / 2.0F,
                        top - cornerSize,
                        arrowWidth / 2.0F,
                        top
                },
                new float[]{
                        3.0F * uvStep, 2.0F * uvStep,
                        4.0F * uvStep, 3.0F * uvStep
                }
        );
    }
}
