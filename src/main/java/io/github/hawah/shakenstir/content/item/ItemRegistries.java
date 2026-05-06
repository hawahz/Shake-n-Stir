package io.github.hawah.shakenstir.content.item;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.dataComponent.ShakeContentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

@EventBusSubscriber
public class ItemRegistries {
    public static final DeferredRegister.Items ITEM = DeferredRegister.createItems(ShakenStir.MODID);
    public static final DeferredItem<PriorityBlockItem> SHAKE = register("shake", (Function<Item.Properties, PriorityBlockItem>) ShakeItem::new);
    public static final DeferredItem<PriorityBlockItem> SHAKE_CUP = register("shake_cup", BlockRegistries.SHAKE_CUP_BLOCK);

    public static <T extends Item> DeferredItem<T> register(String name, Function<Item.Properties, T> supply) {
        return ITEM.register(name, (registryName) -> supply.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))));
    }
    public static <T extends Block> DeferredItem<PriorityBlockItem> register(String name, DeferredBlock<T> block) {
        return ITEM.register(name, (registryName) -> new PriorityBlockItem(block.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))));
    }
    public static <T extends Block> DeferredItem<PriorityBlockItem> register(String name, T block) {
        return ITEM.register(name, (registryName) -> new PriorityBlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))));
    }

    public static void register(IEventBus eventBus) {
        ITEM.register(eventBus);
    }

    @SubscribeEvent
    public static void registerCapability(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.Item.ITEM,
                (itemStack, itemAccess) -> new ShakeContentHolder(itemAccess, DataComponents.CONTAINER, 6),
                ItemRegistries.SHAKE
        );
    }
}
