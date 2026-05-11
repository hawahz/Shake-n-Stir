package io.github.hawah.shakenstir.foundation.events;

import io.github.hawah.shakenstir.content.item.ShakeItem;
import io.github.hawah.shakenstir.foundation.item.ITooltipItem;
import io.github.hawah.shakenstir.util.TooltipHandler;
import net.minecraft.client.gui.screens.worldselection.DataPackReloadCookie;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

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
        TooltipHandler.tryAppendShakingFlagDirect(event, stack);
        TooltipHandler.tryAppendShakeTooltips(stack, event);
    }
}
