package io.github.hawah.shakenstir.client.event;

import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.WarpedMint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;

import java.util.List;

@EventBusSubscriber(Dist.CLIENT)
public class ClientGuiViewEvents {
    @SubscribeEvent
    public static void onRenderContainer(ContainerScreenEvent.Render.Foreground event) {
        var containerScreen = event.getContainerScreen();
        Slot hoveredSlot = containerScreen.getHoveredSlot();
        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        if (hoveredSlot != null && hoveredSlot.getItem().has(DataComponentTypeRegistries.WARPED_MINT)) {
            ItemStack item = hoveredSlot.getItem();
            WarpedMint warpedMint = item.get(DataComponentTypeRegistries.WARPED_MINT);
            int index = item.getOrDefault(DataComponentTypeRegistries.SELECT_MINT, 0);
            int slotSize = 22;
            int offset = index * slotSize;
            int x = hoveredSlot.x;
            int y = hoveredSlot.y - offset;
            Font font = Minecraft.getInstance().font;
            for (int i = 0; i < warpedMint.variety(); i++) {
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        Identifier.withDefaultNamespace("textures/gui/sprites/hud/hotbar_offhand_left.png"),
                        x - 1 - 2,
                        y - 1 - 3 + slotSize * i,
                        0,
                        1,
                        slotSize,
                        slotSize,
                        29,
                        24
                );
            }
            if (index >= 0){
                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        ClientGeneralEvents.HOTBAR_SELECTION_SPRITE,
                        x - 4,
                        y - 4 + slotSize * index,
                        24,
                        23
                );
            }
            List<ItemStack> contents = warpedMint.contents();
            for (int i = 0, contentsSize = contents.size(); i < contentsSize; i++) {
                ItemStack content = contents.get(i);
                graphics.item(
                        content,
                        x,
                        y + slotSize * i
                );
                int len = content.getCount();
                String ctx;
                if (len >= 1000_000_000) {
                    // ctx = len/1000 + "B";
                    ctx = "inf";
                } else if (len >= 1000_000) {
                    ctx = len/1000 + "M";
                } else if (len >= 1000) {
                    ctx = len/1000 + "K";
                } else {
                    ctx = String.valueOf(len);
                }
                graphics.itemDecorations(
                        font,
                        content,
                        x,
                        y + slotSize * i,
                        ctx
                );
            }
        }
    }
}
