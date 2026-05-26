package io.github.hawah.shakenstir.client.clientTooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2fStack;

public record ClientProgressBarTooltip(double progress, int width, int ofX, int ofY) implements ClientTooltipComponent {

    @Override
    public int getHeight(Font font) {
        return font.lineHeight + ofY();
    }

    @Override
    public int getWidth(Font font) {
        return Math.max(font.width(new StringBuilder().repeat('|', width()).toString()), font.width(".")) + ofX();
    }

    @Override
    public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        ClientTooltipComponent.super.extractText(graphics, font, x, y);
        int t = (int) (progress * width());
        int invertT = width() - t - 1;
        // TODO Configured
        float delta = (int)((progress() * width() - t) * 10)/10F;
        int lerpColor = ARGB.linearLerp(delta, ChatFormatting.DARK_GRAY.getColor(), ChatFormatting.GREEN.getColor());
//        graphics.text(font,
//                Component.literal(new StringBuilder().repeat("|", Math.max(0, t)).toString()).withStyle(ChatFormatting.GREEN)
//                        .append(Component.literal("|").withColor(lerpColor))
//                        .append(Component.literal(new StringBuilder().repeat(".", Math.max(0, invertT)).toString()).withStyle(ChatFormatting.DARK_GRAY)),
//                x + ofX,
//                y + ofY,
//                0xFFFFFFFF
//        );
        graphics.text(font,
                Component.literal(new StringBuilder().repeat("|", Math.max(0, t)).toString()).withStyle(ChatFormatting.GREEN),
                x + ofX,
                y + ofY,
                0xFFFFFFFF
        );
        if (delta != 0){
            Matrix3x2fStack pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(0, y + ofY + font.lineHeight - 2);
            pose.scale(1, delta);
            pose.translate(0, -font.lineHeight + 2);
            graphics.text(font,
                    Component.literal("|").withColor(lerpColor),
                    x + ofX + Math.max(0, t) * 2,
                    0,
                    0xFFFFFFFF
            );
            pose.popMatrix();
        } else {
            graphics.text(font,
                    Component.literal(".").withColor(lerpColor),
                    x + ofX + Math.max(0, t) * 2,
                    y + ofY,
                    0xFFFFFFFF
            );
        }
        graphics.text(font,
                Component.literal(new StringBuilder().repeat(".", Math.max(0, invertT)).toString()).withStyle(ChatFormatting.DARK_GRAY),
                x + ofX + Math.max(1, t + 1) * 2,
                y + ofY,
                0xFFFFFFFF
        );
    }
}
