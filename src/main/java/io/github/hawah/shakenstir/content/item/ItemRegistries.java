package io.github.hawah.shakenstir.content.item;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ItemRegistries {
    public static final DeferredRegister.Items ITEM = DeferredRegister.createItems(ShakenStir.MODID);

    public static <T extends Item> DeferredItem<T> register(String name, Function<Item.Properties, T> supply) {
        return ITEM.register(name, () -> supply.apply(new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        ITEM.register(eventBus);
    }
}
