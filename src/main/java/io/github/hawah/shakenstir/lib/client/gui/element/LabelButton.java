package io.github.hawah.shakenstir.lib.client.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import java.awt.*;

public class LabelButton extends Button {
    public LabelButton(Builder builder) {
        super(builder);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        new Color(0x00CF68);
        int i = !this.isHovered()?
                this.isActive()?
                        0xBCBCBC :
                        0x00CF68 :
                0xFFFFFF;
//        this.renderString(guiGraphics, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }
}
