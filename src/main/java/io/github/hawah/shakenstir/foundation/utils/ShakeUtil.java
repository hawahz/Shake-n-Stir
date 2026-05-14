package io.github.hawah.shakenstir.foundation.utils;

import io.github.hawah.shakenstir.content.dataComponent.*;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class ShakeUtil {
    public static boolean hasFluid(DataComponentGetter itemStack) {
        return !itemStack.getOrDefault(DataComponentTypeRegistries.SHAKE_CONTENT, ShakeContentHolder.EMPTY).fluidStacks().isEmpty();
    }

    public static boolean hasItem(DataComponentGetter itemStack) {
        return !itemStack.getOrDefault(DataComponentTypeRegistries.SHAKE_CONTENT, ShakeContentHolder.EMPTY).itemStacks().isEmpty();
    }

    public static List<FluidStack> getFluidStacks(DataComponentGetter itemStack) {
        return itemStack.getOrDefault(DataComponentTypeRegistries.SHAKE_CONTENT, ShakeContentHolder.EMPTY).fluidStacks();
    }

    public static List<ItemStack> getItemStacks(DataComponentGetter itemStack) {
        return itemStack.getOrDefault(DataComponentTypeRegistries.SHAKE_CONTENT, ShakeContentHolder.EMPTY).itemStacks();
    }

    public static boolean isTooltipValid(ItemStack itemStack) {
        return hasFluid(itemStack) || hasItem(itemStack);
    }

    public static IFluidDataHolder getFluidData(DataComponentGetter itemStack) {
        return itemStack.getOrDefault(DataComponentTypeRegistries.SHAKE_CONTENT, ShakeContentHolder.EMPTY);
    }

    public static IItemDataHolder getItemData(DataComponentGetter getter) {
        return getter.getOrDefault(DataComponentTypeRegistries.SHAKE_CONTENT, ShakeContentHolder.EMPTY);
    }

    public static void setFluidData(MutableDataComponentHolder componentGetter, List<FluidStack> fluidStacks) {
        IItemDataHolder itemData = getItemData(componentGetter);
        if (fluidStacks.isEmpty() && itemData.itemStacks().isEmpty()) {
            clearContent(componentGetter);
            return;
        }
        componentGetter.set(DataComponentTypeRegistries.SHAKE_CONTENT, ShakeContentHolder.of(fluidStacks.stream().filter(f -> !f.isEmpty()).toList(), itemData.itemStacks()));
    }

    public static void setItemData(MutableDataComponentHolder componentHolder, List<ItemStack> itemStacks) {
        IFluidDataHolder fluidData = getFluidData(componentHolder);
        if (itemStacks.isEmpty() && fluidData.fluidStacks().isEmpty()) {
            clearContent(componentHolder);
            return;
        }
        componentHolder.set(DataComponentTypeRegistries.SHAKE_CONTENT, ShakeContentHolder.of(fluidData.fluidStacks(), itemStacks.stream().filter(f -> !f.isEmpty()).toList()));
    }

    public static void clearFluidData(MutableDataComponentHolder componentHolder) {
        setFluidData(componentHolder, List.of());
    }

    public static void clearItemData(MutableDataComponentHolder holder) {
        setItemData(holder, List.of());
    }

    public static void clearContent(MutableDataComponentHolder holder) {
        holder.remove(DataComponentTypeRegistries.SHAKE_CONTENT);
    }
}
