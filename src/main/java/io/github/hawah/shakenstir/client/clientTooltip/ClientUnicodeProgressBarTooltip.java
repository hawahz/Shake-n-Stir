package io.github.hawah.shakenstir.client.clientTooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

@Deprecated
public record ClientUnicodeProgressBarTooltip(double progress, int width, int ofX, int ofY) implements ClientTooltipComponent {
    public static final char[] chs = new char[]{'▏', '▎', '▍', '▌', '▋', '▊', '▉', '█'};

    @Override
    public int getHeight(Font font) {
        return font.lineHeight;
    }

    @Override
    public int getWidth(Font font) {
        System.out.println(font.width("█"));
        return font.width(new StringBuilder().repeat('█', width()).toString());
    }

    @Override
    public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {

        ClientTooltipComponent.super.extractText(graphics, font, x, y);
        int t = (int) (progress * width());
        int invertT = width() - t - 1;
        double minProgress = progress() * width() - t;
        int lerpColor = ARGB.linearLerp((float) (progress() * width() - t), ChatFormatting.DARK_GRAY.getColor(), ChatFormatting.GREEN.getColor());
        graphics.text(
                font,
                Component.literal(new StringBuilder().repeat('█', t).toString()).withStyle(ChatFormatting.GREEN)
                        .append(String.valueOf(chs[(int) (minProgress * chs.length)])),
                x + ofX,
                y + ofY,
                -1
        );
        graphics.text(
                font,
                String.valueOf(chs),
                x + ofX,
                y + ofY + font.lineHeight,
                -1
        );
//        graphics.text(font,
//                Component.literal(new StringBuilder().repeat("|", Math.max(0, t)).toString()).withStyle(ChatFormatting.GREEN)
//                        .append(Component.literal("|").withColor(lerpColor))
//                        .append(Component.literal(new StringBuilder().repeat("|", Math.max(0, invertT)).toString()).withStyle(ChatFormatting.DARK_GRAY)),
//                x + ofX,
//                y + ofY,
//                0xFFFFFFFF
//        );
    }
}
