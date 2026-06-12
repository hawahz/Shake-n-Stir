package io.github.hawah.shakenstir.util;

import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import net.neoforged.neoforge.event.AddAttributeTooltipsEvent;

import java.util.function.Consumer;

public class TooltipHandler {
    public static void tryAppendShakingFlagDirect(AddAttributeTooltipsEvent event, ItemStack stack) {
        if (stack.has(DataComponentTypeRegistries.SHAKING)) {
            event.addTooltipLines(LangData.TOOLTIP_SHAKE_SHAKING.get());
        }
    }

    public static <T extends TooltipProvider> void addToTooltip(
            ItemStack itemStack, DataComponentType<T> type, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> consumer, TooltipFlag flag
    ) {
        T component = itemStack.get(type);
        if (component != null && display.shows(type)) {
            component.addToTooltip(context, consumer, flag, itemStack.getComponents());
        }
    }

    /*
    TODO
     Bartender Glove,
     Recipe Scroll,
     SOBERING_TEA,
     RAG,
     MENU

     */
    public static <T> void addToTooltipC(
            ItemStack itemStack, DataComponentType<T> type, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> consumer, TooltipFlag flag
    ) {
        T component = itemStack.get(type);
        if (component != null && display.shows(type)) {
            if (DataComponentTypeRegistries.BARTENDER_GLOVE.equals(type)) {

            }
        }
    }
}
