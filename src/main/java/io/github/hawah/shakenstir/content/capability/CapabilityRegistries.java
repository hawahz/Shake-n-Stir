package io.github.hawah.shakenstir.content.capability;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber
public class CapabilityRegistries {
    @SubscribeEvent
    public static void onRegisterCapability(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.Fluid.ITEM, (stack, access) -> {
                    if (stack.is(Items.POTION) && !stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)) {
                        return null;
                    }
                    return new BottleResourceHandler(access);
                },
                Items.POTION,
                Items.HONEY_BOTTLE
        );
    }
}
