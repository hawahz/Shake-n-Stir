package io.github.hawah.shakenstir.util;

import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import io.github.hawah.shakenstir.foundation.utils.ShakeUtil;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class TooltipHandler {
    public static void tryAppendShakingFlagDirect(AddAttributeTooltipsEvent event, ItemStack stack) {
        if (stack.getOrDefault(DataComponentTypeRegistries.SHAKING, false)) {
            event.addTooltipLines(LangData.TOOLTIP_SHAKE_SHAKING.get());
        }
    }

    public static void tryAppendShakeTooltips(ItemStack shakeStack, AddAttributeTooltipsEvent event) {
        if (ShakeUtil.isTooltipValid(shakeStack)) {
            return;
        }
        List<ItemStack> itemStacks = ShakeUtil.getItemStacks(shakeStack);
        List<FluidStack> fluidFromShake = ShakeUtil.getFluidStacks(shakeStack);
        boolean shaking = shakeStack.getOrDefault(DataComponentTypeRegistries.SHAKING, false);
        boolean holdingProduct = !itemStacks.isEmpty() && itemStacks.getFirst().is(ItemRegistries.CONTENT_HOLDER);
        boolean validShaking = holdingProduct && shaking;
        List<ItemStack> items;
        List<FluidStack> fluidStacks;
        if (validShaking) {
            items = ShakeUtil.getItemStacks(itemStacks.getFirst());
            fluidStacks = ShakeUtil.getFluidStacks(itemStacks.getFirst());
        } else {
            items = itemStacks;
            fluidStacks = fluidFromShake;
        }
        if (holdingProduct && !shaking) {
            ItemStack first = itemStacks.getFirst();
            event.addTooltipLines(first.getHoverName());
            return;
        }
        if (!fluidStacks.isEmpty()) {
            event.addTooltipLines(LangData.TOOLTIP_SHAKE_FLUID_CONTENT.get());
        }
        for (FluidStack fluidStack : fluidStacks) {
            event.addTooltipLines(fluidStack.getHoverName());
        }
        if (!items.isEmpty()) {
            event.addTooltipLines(LangData.TOOLTIP_SHAKE_CONTENT.get());
        }
        for (ItemStack item : items) {
            event.addTooltipLines(item.getHoverName());
        }

    }
}
