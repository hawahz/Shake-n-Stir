package io.github.hawah.shakenstir.foundation.events;

import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.item.ShakeItem;
import io.github.hawah.shakenstir.util.TooltipHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.function.Consumer;

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

        AttributeTooltipContext ctx         = event.getContext();
        TooltipDisplay display              = ctx.tooltipDisplay();
        Consumer<Component> linesConsumer   = event::addTooltipLines;
        TooltipFlag tooltipFlag             = ctx.flag();

        TooltipHandler.tryAppendShakingFlagDirect   (event, stack);
//        TooltipHandler.tryAppendShakeTooltips       (event, stack);
//        TooltipHandler.addToTooltip(stack, DataComponentTypeRegistries.SHAKE_CONTENT        , ctx, display, linesConsumer, tooltipFlag);
        TooltipHandler.addToTooltip(stack, DataComponentTypeRegistries.SPIRIT_CONTENT       , ctx, display, linesConsumer, tooltipFlag);
        TooltipHandler.addToTooltip(stack, DataComponentTypeRegistries.SHAKE_PRODUCT_QUALITY, ctx, display, linesConsumer, tooltipFlag);
        TooltipHandler.addToTooltip(stack, DataComponentTypeRegistries.DRINK_DATA           , ctx, display, linesConsumer, tooltipFlag);
//        TooltipHandler.addToTooltip(stack, DataComponentTypeRegistries.SPIRIT_CONTENT       , ctx, display, linesConsumer, tooltipFlag);
    }

//    @SubscribeEvent
//    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
//        event.registerBlock(
//                Capabilities.Fluid.BLOCK
//        );
//    }
}
