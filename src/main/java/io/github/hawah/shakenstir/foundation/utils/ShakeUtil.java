package io.github.hawah.shakenstir.foundation.utils;

import io.github.hawah.shakenstir.content.dataComponent.*;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.List;

public class ShakeUtil {

    public static ShakeContentHolder get(DataComponentGetter getter) {
        return getter.getOrDefault(DataComponentTypeRegistries.SHAKE_CONTENT, ShakeContentHolder.EMPTY);
    }
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

    public static int getIceCount(DataComponentGetter getter) {
        return getter.getOrDefault(DataComponentTypeRegistries.SHAKE_ICE_CUBES, 0);
    }

    public static boolean hasCup(DataComponentGetter getter) {
        return getter.getOrDefault(DataComponentTypeRegistries.HAS_CUP, false);
    }

    public static int rgbWithWeight(Pair<Integer, Integer>... rgbWeights) {
        return rgbWithWeight(Arrays.stream(rgbWeights).toList());
    }

    public static int rgbWithWeight(List<Pair<Integer, Integer>> rgbWeights) {
        return argbWithWeight(rgbWeights.stream().map((integerIntegerPair ->
                Pair.of(integerIntegerPair.first() & ~ 0xFF000000 | 0xFF000000, integerIntegerPair.right())
        )).toList());
    }

    public static int argbWithWeight(Pair<Integer, Integer>... rgbWeights) {
        return argbWithWeight(Arrays.stream(rgbWeights).toList());
    }

    public static int argbWithWeight(List<Pair<Integer, Integer>> rgbWeights) {
        if (rgbWeights.isEmpty()) {
            return 0xFFFFFFFF;
        }
        long weightSum = rgbWeights.stream().mapToInt(Pair::right).sum();
        int firstARGB = rgbWeights.getFirst().left();
        float firstWeight = rgbWeights.getFirst().right();
        float a = ARGB.alpha(firstARGB) * firstWeight / weightSum;
        float r = ARGB.red(firstARGB) * firstWeight / weightSum;
        float g = ARGB.green(firstARGB) * firstWeight / weightSum;
        float b = ARGB.blue(firstARGB) * firstWeight / weightSum;
        for (int i = 1; i < rgbWeights.size(); i++) {
            int color = rgbWeights.get(i).left();
            float weight = rgbWeights.get(i).right();
            a += ARGB.alpha(color) * weight / weightSum;
            r += ARGB.red(color) * weight / weightSum;
            g += ARGB.green(color) * weight / weightSum;
            b += ARGB.blue(color) * weight /weightSum;
        }

        return ARGB.color((int) a, (int) r, (int) g, (int) b);
    }
}
