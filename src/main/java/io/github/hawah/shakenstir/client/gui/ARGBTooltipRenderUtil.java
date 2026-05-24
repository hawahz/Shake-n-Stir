package io.github.hawah.shakenstir.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ARGBTooltipRenderUtil {
    private static final Identifier BACKGROUND_SPRITE = Identifier.withDefaultNamespace("tooltip/background");
    private static final Identifier FRAME_SPRITE = Identifier.withDefaultNamespace("tooltip/frame");
    public static final int MOUSE_OFFSET = 12;
    private static final int PADDING = 3;
    public static final int PADDING_LEFT = 3;
    public static final int PADDING_RIGHT = 3;
    public static final int PADDING_TOP = 3;
    public static final int PADDING_BOTTOM = 3;
    private static final int MARGIN = 9;

    public static void extractTooltipBackground(GuiGraphicsExtractor graphics, int x, int y, int w, int h, @Nullable Identifier style, int color) {
        int x0 = x - 3 - 9;
        int y0 = y - 3 - 9;
        int paddedWidth = w + 3 + 3 + 18;
        int paddedHeight = h + 3 + 3 + 18;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, getBackgroundSprite(style), x0, y0, paddedWidth, paddedHeight, color);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, getFrameSprite(style), x0, y0, paddedWidth, paddedHeight);
    }

    private static Identifier getBackgroundSprite(@Nullable Identifier style) {
        return style == null ? BACKGROUND_SPRITE : style.withPath(path -> "tooltip/" + path + "_background");
    }

    private static Identifier getFrameSprite(@Nullable Identifier style) {
        return style == null ? FRAME_SPRITE : style.withPath(path -> "tooltip/" + path + "_frame");
    }

    public static void tooltip(GuiGraphicsExtractor graphics, Font font, List<ClientTooltipComponent> lines, int xo, int yo, ClientTooltipPositioner positioner, @Nullable Identifier style, int color) {
        tooltip(graphics, font, lines, xo, yo, positioner, style, ItemStack.EMPTY, color);
    }

    public static void tooltip(GuiGraphicsExtractor graphics, Font font, List<ClientTooltipComponent> lines, int xo, int yo, ClientTooltipPositioner positioner, @Nullable Identifier style, ItemStack tooltipStack, int color) {
        var preEvent = net.neoforged.neoforge.client.ClientHooks.onRenderTooltipPre(tooltipStack, graphics, xo, yo, graphics.guiWidth(), graphics.guiHeight(), lines, font, positioner);
        if (preEvent.isCanceled()) return;

        font = preEvent.getFont();
        xo = preEvent.getX();
        yo = preEvent.getY();

        int textWidth = 0;
        int tempHeight = lines.size() == 1 ? -2 : 0;

        for (ClientTooltipComponent line : lines) {
            int lineWidth = line.getWidth(font);
            if (lineWidth > textWidth) {
                textWidth = lineWidth;
            }

            tempHeight += line.getHeight(font);
        }

        int w = textWidth;
        int h = tempHeight;
        Vector2ic positionedTooltip = positioner.positionTooltip(graphics.guiWidth(), graphics.guiHeight(), xo, yo, textWidth, tempHeight);
        int x = positionedTooltip.x();
        int y = positionedTooltip.y();
        graphics.pose().pushMatrix();
        var textureEvent = net.neoforged.neoforge.client.ClientHooks.onRenderTooltipTexture(tooltipStack, graphics, x, y, preEvent.getFont(), lines, style);
        extractTooltipBackground(graphics, x, y, textWidth, tempHeight, textureEvent.getTexture(), color);
        int localY = y;

        for (int i = 0; i < lines.size(); i++) {
            ClientTooltipComponent line = lines.get(i);
            line.extractText(graphics, font, x, localY);
            localY += line.getHeight(font) + (i == 0 ? 2 : 0);
        }

        localY = y;

        for (int i = 0; i < lines.size(); i++) {
            ClientTooltipComponent line = lines.get(i);
            line.extractImage(font, x, localY, w, h, graphics);
            localY += line.getHeight(font) + (i == 0 ? 2 : 0);
        }

        graphics.pose().popMatrix();
    }
}
