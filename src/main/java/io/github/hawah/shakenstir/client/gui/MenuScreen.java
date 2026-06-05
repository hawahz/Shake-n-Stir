package io.github.hawah.shakenstir.client.gui;

import io.github.hawah.shakenstir.content.blockEntity.BarMenuBlockEntity;
import io.github.hawah.shakenstir.foundation.networking.ServerboundMenuBEChanged;
import io.github.hawah.shakenstir.lib.networking.Networking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MenuScreen extends AbstractMenuScreen {

    public MenuScreen(BarMenuBlockEntity cachedBlockEntity) {
        super(cachedBlockEntity);
    }

    @Override
    protected void renderWindowPre(GuiGraphicsExtractor guiGraphics, int originMouseX, int originMouseY, float partialTick) {
        super.renderWindowPre(guiGraphics, originMouseX, originMouseY, partialTick);
        int currentSelect = getCurrentSelect(originMouseX, originMouseY);
        if (currentSelect >= 0) {
            List<Component> list = new ArrayList<>();
            cachedBlockEntity.recipes.get(currentIndex).left().addToTooltip(
                    Item.TooltipContext.EMPTY,
                    list::add,
                    TooltipFlag.NORMAL,
                    ItemStack.EMPTY.getComponents()
            );
            guiGraphics.setTooltipForNextFrame(
                    this.minecraft.font,
                    list,
                    Optional.empty(),
                    originMouseX,
                    originMouseY
            );
        }
    }

    @Override
    protected void renderWindowPost(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWindowPost(guiGraphics, mouseX, mouseY, partialTick);
    }
    double count = 0;
    int prevSelect = -1;
    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        int currentSelect = getCurrentSelect((int) x, (int) y);
        boolean changed = prevSelect != currentSelect;
        prevSelect = currentSelect;
        if (currentSelect >= 0 && currentSelect < cachedBlockEntity.recipes.size()) {
            if (changed) {
                count = cachedBlockEntity.recipes.get(currentIndex).right().count;
            }
            count += scrollY;
            count = Math.max(0, count);
            cachedBlockEntity.setRecipeCount(currentSelect, (int) count);
            Networking.sendToServer(new ServerboundMenuBEChanged(cachedBlockEntity.recipes.get(currentIndex).right(), currentIndex, cachedBlockEntity.getBlockPos()));
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }
}
