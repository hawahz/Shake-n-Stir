package io.github.hawah.shakenstir.content.item;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

@EventBusSubscriber
public class MintItem extends Item {
    public MintItem(Properties properties, int idx) {
        super(properties.component(DataComponentTypeRegistries.DECORATE_MODEL, ShakenStir.asResource("mint_deco_" + idx)));
    }

    @SubscribeEvent
    public static void onPlayerPickItem(ItemEntityPickupEvent.Post event) {
        if (event.getOriginalStack().getItem() instanceof MintItem) {
            event.getCurrentStack().setCount(0);
        }
    }
}
