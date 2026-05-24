package io.github.hawah.shakenstir.client.clientTooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public record ItemTooltipWithNameAndCount(ItemStack itemStack, int ofX, int ofY) implements ClientTooltipComponent {
    @Override
    public int getHeight(Font font) {
        return 16;
    }

    @Override
    public int getWidth(Font font) {
        return 16;
    }

    @Override
    public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        ClientTooltipComponent.super.extractText(graphics, font, x, y);
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
        ClientTooltipComponent.super.extractImage(font, x, y, w, h, graphics);
        extractItem(font, x + ofX, y + ofY, graphics);
    }

    private void extractItem(Font font, int x, int y, GuiGraphicsExtractor graphics) {
        graphics.item(itemStack(), x, y);
        graphics.itemDecorations(font, itemStack(), x, y);
    }
}
