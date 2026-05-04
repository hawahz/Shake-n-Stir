package io.github.hawah.shakenstir.lib.client.gui.element;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class GuiAnimateElement {

    float animateTick = 0;
    public boolean activate;

    void active() {
        activate = true;
    }
    int getAnimateTick() {
        return (int) animateTick;
    }
    public abstract void tick();
    public abstract void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks);
}
