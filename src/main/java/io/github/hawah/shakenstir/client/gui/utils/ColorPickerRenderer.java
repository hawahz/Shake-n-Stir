package io.github.hawah.shakenstir.client.gui.utils;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public record ColorPickerRenderer(
        Matrix3x2f pose,
        int x,
        int y,
        int width,
        int height,
        int hue
) implements GuiElementRenderState {
    /**
     * 绘制S-V色板：横向为饱和度(0→100)，纵向为明度(100→0)，色相固定
     */
    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        int stepX = Math.max(1, width / 2);
        int stepY = Math.max(1, height / 2);
        float cellW = (float) width / stepX;
        float cellH = (float) height / stepY;
        float px = (float) this.x();
        float py = (float) this.y();
        int h = this.hue();

        for (int row = 0; row < stepY; row++) {
            float y0 = py + row * cellH;
            float y1 = py + (row + 1) * cellH;

            int v0 = 100 - row * 100 / stepY;
            int v1 = 100 - (row + 1) * 100 / stepY;

            for (int col = 0; col < stepX; col++) {
                float x0 = px + col * cellW;
                float x1 = px + (col + 1) * cellW;

                int s0 = col * 100 / stepX;
                int s1 = (col + 1) * 100 / stepX;

                int tl = ColorPicker.hsvToARGB(h, s0, v0);
                int bl = ColorPicker.hsvToARGB(h, s0, v1);
                int br = ColorPicker.hsvToARGB(h, s1, v1);
                int tr = ColorPicker.hsvToARGB(h, s1, v0);

                vertexConsumer.addVertexWith2DPose(this.pose(), x0, y0).setColor(tl);
                vertexConsumer.addVertexWith2DPose(this.pose(), x0, y1).setColor(bl);
                vertexConsumer.addVertexWith2DPose(this.pose(), x1, y1).setColor(br);
                vertexConsumer.addVertexWith2DPose(this.pose(), x1, y0).setColor(tr);
            }
        }
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI;
    }

    @Override
    public TextureSetup textureSetup() {
        return new TextureSetup(
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        return new ScreenRectangle(x, y, width(), height);
    }
}
