package io.github.hawah.shakenstir.foundation.events;

import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.ShakeFluidDataComponent;
import io.github.hawah.shakenstir.content.dataComponent.ShakeItemDataComponent;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.item.ShakeItem;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import io.github.hawah.shakenstir.foundation.item.ITooltipItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

@EventBusSubscriber
public class ShakingEvents {
    @SubscribeEvent
    public static void onPlayerHurt(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.isUsingItem() && entity.getUseItem().getItem() instanceof ShakeItem) {
            if (entity instanceof Player player) {
                player.getCooldowns().addCooldown(entity.getUseItem(), 20);
            }
            entity.stopUsingItem();
        }
    }

    @SubscribeEvent
    public static void onAddTooltips(AddAttributeTooltipsEvent event) {
        ItemStack stack = event.getStack();
        if (stack.getItem() instanceof ITooltipItem tooltipItem) {
            tooltipItem.appendHoverText(event);
        }
        tryAppendShakingFlagDirect(event, stack);
        tryAppendShakeTooltips(stack, event);
    }

    private static void tryAppendShakingFlagDirect(AddAttributeTooltipsEvent event, ItemStack stack) {
        if (stack.getOrDefault(DataComponentTypeRegistries.SHAKING, false)) {
            event.addTooltipLines(LangData.TOOLTIP_SHAKE_SHAKING.get());
        }
    }

    private static void tryAppendShakeTooltips(ItemStack shakeStack, AddAttributeTooltipsEvent event) {
        if (!shakeStack.has(DataComponentTypeRegistries.SHAKE_ITEM_INGREDIENT) && !shakeStack.has(DataComponentTypeRegistries.SHAKE_CONTENT)) {
            return;
        }
        List<ItemStack> itemStacks = shakeStack.getOrDefault(DataComponentTypeRegistries.SHAKE_ITEM_INGREDIENT, ShakeItemDataComponent.EMPTY).itemStacks();
        List<FluidStack> fluidFromShake = shakeStack.getOrDefault(DataComponentTypeRegistries.SHAKE_CONTENT, ShakeFluidDataComponent.EMPTY).fluidStacks();
        boolean shaking = shakeStack.getOrDefault(DataComponentTypeRegistries.SHAKING, false);
        boolean holdingProduct = !itemStacks.isEmpty() && itemStacks.getFirst().is(ItemRegistries.CONTENT_HOLDER);
        boolean validShaking = holdingProduct && shaking;
        List<ItemStack> items;
        List<FluidStack> fluidStacks;
        if (validShaking) {
            event.addTooltipLines(LangData.TOOLTIP_SHAKE_SHAKING.get());
            items = itemStacks.getFirst().getOrDefault(DataComponentTypeRegistries.SHAKE_ITEM_INGREDIENT, ShakeItemDataComponent.EMPTY).itemStacks();
            fluidStacks = itemStacks.getFirst().getOrDefault(DataComponentTypeRegistries.SHAKE_CONTENT, ShakeFluidDataComponent.EMPTY).fluidStacks();
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

//    @SubscribeEvent
//    public static void onDataPacketRegistries(DataPackRegistryEvent event) {
//    }
}
