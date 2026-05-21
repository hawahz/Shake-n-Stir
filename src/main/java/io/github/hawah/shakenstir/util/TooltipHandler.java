package io.github.hawah.shakenstir.util;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.recipe.Quality;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import io.github.hawah.shakenstir.foundation.datapack.DrinkData;
import io.github.hawah.shakenstir.foundation.utils.ShakeUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;

public class TooltipHandler {
    public static void tryAppendShakingFlagDirect(AddAttributeTooltipsEvent event, ItemStack stack) {
        if (stack.getOrDefault(DataComponentTypeRegistries.SHAKING, false)) {
            event.addTooltipLines(LangData.TOOLTIP_SHAKE_SHAKING.get());
        }
    }
    public static void tryAppendQuality(AddAttributeTooltipsEvent event, ItemStack stack) {
        if (stack.has(DataComponentTypeRegistries.SHAKE_PRODUCT_QUALITY)) {
            event.addTooltipLines(stack.getOrDefault(DataComponentTypeRegistries.SHAKE_PRODUCT_QUALITY, Quality.AVERAGE).getTooltip());
        }
    }

    public static void tryAppendDrinkTooltips(AddAttributeTooltipsEvent event, ItemStack drinkStack) {
        if (!drinkStack.has(DataComponentTypeRegistries.DRINK_DATA)) {
            return;
        }
        startPotion();
        boolean moreInformation = event.getContext().flag().hasShiftDown();
        DrinkData drinkData = drinkStack.get(DataComponentTypeRegistries.DRINK_DATA);
        MutableComponent translate = drinkData.type().translate(List.of(), List.of());
        String string = translate.getString().replaceAll("^\\s+|\\s+", " ").trim();
        event.addTooltipLines(LangData.TOOLTIP_TITLE_COCKTAIL.get(string));
        Quality quality = drinkData.quality();
        if (moreInformation) {
            List<MobEffectInstance> cocktailEffects = drinkData.type().effects().stream().map(effectData -> effectData.get(quality)).toList();
            if (!cocktailEffects.isEmpty()) {
                PotionContents.addPotionTooltip(
                        cocktailEffects,
                        event::addTooltipLines,
                        1.0F,
                        event.getContext().tickRate()
                );
            }
        }
        FluidStack fluidStack = new FluidStack(drinkData.base().fluidType().value(), 250);
        event.addTooltipLines(LangData.TOOLTIP_TITLE_BASE.get(fluidStack.getHoverName()));
        if (moreInformation) {
            addPotionTooltip(
                    drinkData.baseEffects(),
                    event::addTooltipLines,
                    1.0F,
                    event.getContext().tickRate()
            );
        }
        event.addTooltipLines(LangData.TOOLTIP_TITLE_DRUNK_LEVEL.get(drinkData.drunkLevel()));
        if (moreInformation) {
            addPotionTooltip(
                    drinkData.drunkEffects(),
                    event::addTooltipLines,
                    1.0F,
                    event.getContext().tickRate()
            );
        }
        event.addTooltipLines(LangData.TOOLTIP_TITLE_ICE_LEVEL.get(drinkData.coldLevel()));
        if (moreInformation) {
            addPotionTooltip(
                    drinkData.coldEffects(),
                    event::addTooltipLines,
                    1.0F,
                    event.getContext().tickRate()
            );
        }
        if (!moreInformation) {
            event.addTooltipLines(LangData.SHIFT.get());
        }
        endPotion(event::addTooltipLines);
    }

    public static List<Pair<Holder<Attribute>, AttributeModifier>> modifiers = Lists.newArrayList();

    public static void startPotion() {
        modifiers.clear();
    }
    public static void endPotion(Consumer<Component> lines) {
        if (!modifiers.isEmpty()) {
            lines.accept(CommonComponents.EMPTY);
            lines.accept(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

            // Neo: Override handling of potion attribute tooltips to support IAttributeExtension
            net.neoforged.neoforge.common.util.AttributeUtil.addPotionTooltip(modifiers, lines);
        }
        modifiers.clear();
    }
    public static void addPotionTooltip(Iterable<MobEffectInstance> effects, Consumer<Component> lines, float durationScale, float tickrate) {

        boolean noEffects = true;

        for (MobEffectInstance effect : effects) {
            noEffects = false;
            Holder<MobEffect> mobEffect = effect.getEffect();
            int amplifier = effect.getAmplifier();
            mobEffect.value().createModifiers(amplifier, (attribute, modifierx) -> modifiers.add(new Pair<>(attribute, modifierx)));
            MutableComponent line = PotionContents.getPotionDescription(mobEffect, amplifier);
            if (!effect.endsWithin(20)) {
                line = Component.translatable("potion.withDuration", line, MobEffectUtil.formatDuration(effect, durationScale, tickrate));
            }

            lines.accept(line.withStyle(mobEffect.value().getCategory().getTooltipFormatting()));
        }
    }

    public static void tryAppendShakeTooltips(AddAttributeTooltipsEvent event, ItemStack shakeStack) {
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
