package io.github.hawah.shakenstir.client.gui.utils;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public record HueSliderRenderer(
        Matrix3x2f pose,
        int x,
        int y,
        int width,
        int height
) implements GuiElementRenderState {
    /**
     * 绘制色相滑条：横向为色相(0→100)，纵向固定V=100,S=100
     */
    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        int steps = Math.max(1, width / 2);
        float stripW = (float) width / steps;
        float px = (float) this.x();
        float py = (float) this.y();
        float ph = (float) height;

        for (int i = 0; i < steps; i++) {
            float x0 = px + i * stripW;
            float x1 = px + (i + 1) * stripW;

            int h0 = i * 100 / steps;
            int h1 = (i + 1) * 100 / steps;

            int tl = ColorPicker.hsvToARGB(h0, 100, 100);
            int bl = ColorPicker.hsvToARGB(h0, 100, 0);
            int br = ColorPicker.hsvToARGB(h1, 100, 0);
            int tr = ColorPicker.hsvToARGB(h1, 100, 100);

            vertexConsumer.addVertexWith2DPose(this.pose(), x0, py).setColor(tl);
            vertexConsumer.addVertexWith2DPose(this.pose(), x0, py + ph).setColor(tl);
            vertexConsumer.addVertexWith2DPose(this.pose(), x1, py + ph).setColor(tr);
            vertexConsumer.addVertexWith2DPose(this.pose(), x1, py).setColor(tr);
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
