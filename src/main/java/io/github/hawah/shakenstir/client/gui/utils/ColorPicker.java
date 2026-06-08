package io.github.hawah.shakenstir.client.gui.utils;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.lib.client.gui.BaseScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ColorPicker extends AbstractWidget {

    private static final int SV_W = 42;
    private static final int SV_H = 26;
    private static final int SLIDER_H = 3;
    private static final int PADDING = 3;

    private int h = 50;
    private int s = 100;
    private int v = 100;
    private int color = hsvToARGB(h, s, v);

    public ColorPicker(int x, int y, int width, int height, Component message) {
        super(x, y, 48, 40, message);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.submitGuiElementRenderState(
                new ColorPickerRenderer(graphics.pose(), getX() + 3, getY() + 3, 42, 26, h)
        );

        graphics.submitGuiElementRenderState(
                new HueSliderRenderer(graphics.pose(), getX() + 3, getY() + 34, 42, 3)
        );

        BaseScreen.blit(
                graphics,
                ShakenStir.asResource("textures/gui/menu.png"),
                getX(),
                getY(),
                0,
                64,
                48,
                40
        );

        int indicatorColor = 0x80FFFFFF;

        // S-V色板选取指示（2×2半透明正方形，中心位于所选S,V位置）
        int svCx = getX() + PADDING + s * SV_W / 100;
        int svCy = getY() + PADDING + (100 - v) * SV_H / 100;
        graphics.fill(svCx - 1, svCy - 1, svCx + 1, svCy + 1, indicatorColor);

        // 色相滑条选取指示（2像素宽，覆盖滑条全高）
        int hueCx = getX() + PADDING + h * SV_W / 100;
        int hueTop = getY() + PADDING + SV_H + 5;
        graphics.fill(hueCx - 1, hueTop, hueCx + 1, hueTop + SLIDER_H, indicatorColor);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (pickColor(event.x(), event.y())) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (pickColor(event.x(), event.y())) {
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    /**
     * 根据鼠标坐标从S-V色板或色相滑条选取颜色
     */
    private boolean pickColor(double mouseX, double mouseY) {
        int svLeft = getX() + PADDING;
        int svRight = svLeft + SV_W;
        int svTop = getY() + PADDING;
        int svBottom = svTop + SV_H;

        int hueLeft = getX() + PADDING;
        int hueRight = hueLeft + SV_W;
        int hueTop = getY() + PADDING + SV_H + 5;
        int hueBottom = hueTop + SLIDER_H;

        if (mouseX >= svLeft && mouseX < svRight && mouseY >= svTop && mouseY < svBottom) {
            s = Mth.clamp((int) ((mouseX - svLeft) / (double) SV_W * 100), 0, 100);
            v = Mth.clamp(100 - (int) ((mouseY - svTop) / (double) SV_H * 100), 0, 100);
            updateColor();
            return true;
        }

        if (mouseX >= hueLeft && mouseX < hueRight && mouseY >= hueTop && mouseY < hueBottom) {
            h = Mth.clamp((int) ((mouseX - hueLeft) / (double) SV_W * 100), 0, 100);
            updateColor();
            return true;
        }

        return false;
    }

    private void updateColor() {
        color = hsvToARGB(h, s, v);
    }

    /** @return 当前选取的ARGB颜色 */
    public int getColor() {
        return color;
    }

    /**
     * 将HSV颜色值转换为ARGB整数，各参数最大值为100
     *
     * @param h 色相 (0-100)
     * @param s 饱和度 (0-100)
     * @param v 明度 (0-100)
     * @return ARGB颜色值 (Alpha固定为255)
     */
    public static int hsvToARGB(int h, int s, int v) {
        float hue = (h / 100.0f) * 360.0f;
        float saturation = s / 100.0f;
        float value = v / 100.0f;

        float c = value * saturation;
        float hueSegment = (hue / 60.0f) % 2.0f - 1.0f;
        float x = c * (1.0f - Math.abs(hueSegment));
        float m = value - c;

        float r, g, b;
        if (hue < 60) {
            r = c; g = x; b = 0;
        } else if (hue < 120) {
            r = x; g = c; b = 0;
        } else if (hue < 180) {
            r = 0; g = c; b = x;
        } else if (hue < 240) {
            r = 0; g = x; b = c;
        } else if (hue < 300) {
            r = x; g = 0; b = c;
        } else {
            r = c; g = 0; b = x;
        }

        int red = Math.round((r + m) * 255);
        int green = Math.round((g + m) * 255);
        int blue = Math.round((b + m) * 255);

        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {

    }
}
