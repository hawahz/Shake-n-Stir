package io.github.hawah.shakenstir.client.clientTooltip;

import io.github.hawah.shakenstir.content.WarpedMintTooltip;
import io.github.hawah.shakenstir.content.dataComponent.WarpedMint;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record ClientWarpedMintTooltip(WarpedMint warpedMint, int index) implements ClientTooltipComponent {

    public static final Identifier SLOT = Identifier.withDefaultNamespace("textures/gui/sprites/container/slot.png");
    public static final Identifier HOTBAR_SELECTION_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_selection");

    public ClientWarpedMintTooltip(WarpedMintTooltip tooltip) {
        this(tooltip.warpedMint(), tooltip.index());
    }

    @Override
    public int getHeight(Font font) {
        return 0;
    }

    @Override
    public int getWidth(Font font) {
        return 0;
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
        ClientTooltipComponent.super.extractImage(font, x, y, w, h, graphics);
        if (warpedMint == null) {
            return;
        }
        for (int i = 0; i < warpedMint.variety(); i++) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    Identifier.withDefaultNamespace("textures/gui/sprites/container/slot.png"),
                    x - 1 + 18 * i,
                    y - 1,
                    0,
                    0,
                    18,
                    18,
                    18,
                    18
            );
        }

        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                HOTBAR_SELECTION_SPRITE,
                x - 4+ 18 * index,
                y - 4,
                24,
                23
        );
        List<ItemStack> contents = warpedMint.contents();
        for (int i = 0, contentsSize = contents.size(); i < contentsSize; i++) {
            ItemStack content = contents.get(i);
            graphics.item(
                    content,
                    x + 18 * i,
                    y
            );
            graphics.itemDecorations(
                    font,
                    content,
                    x,
                    y
            );
        }
    }

}
