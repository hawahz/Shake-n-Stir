package io.github.hawah.shakenstir.content.item;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.FluidStackDataComponent;
import io.github.hawah.shakenstir.content.dataComponent.ShakeContentHolder;
import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import io.github.hawah.shakenstir.content.fluid.FluidTypeRegistries;
import io.github.hawah.shakenstir.foundation.item.PriorityBlockItem;
import io.github.hawah.shakenstir.foundation.item.SpiritBottleItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

@EventBusSubscriber
public class ItemRegistries {
    public static final DeferredRegister.Items ITEM = DeferredRegister.createItems(ShakenStir.MODID);
    public static final DeferredItem<PriorityBlockItem> SHAKE = register("shake", (Function<Item.Properties, PriorityBlockItem>) ShakeItem::new);
    public static final DeferredItem<PriorityBlockItem> SHAKE_CUP = register("shake_cup", BlockRegistries.SHAKE_CUP_BLOCK);

    // Spirit
    public static final DeferredItem<SpiritBottleItem> GIN = registerSpirit("gin", BlockRegistries.GIN, new Item.Properties().component(DataComponentTypeRegistries.SPIRIT_CONTENT, new FluidStackDataComponent(new FluidStack(FluidRegistries.GIN_SOURCE_FLUID_BLOCK, 1000))));

    public static <T extends Item> DeferredItem<T> register(String name, Function<Item.Properties, T> supply) {
        return ITEM.register(name, (registryName) -> supply.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))));
    }
    public static <T extends Block> DeferredItem<PriorityBlockItem> register(String name, DeferredBlock<T> block) {
        return ITEM.register(name, (registryName) -> new PriorityBlockItem(block.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))));
    }

    public static <T extends Block> DeferredItem<SpiritBottleItem> registerSpirit(String name, DeferredBlock<T> block, Item.Properties properties) {
        return ITEM.register(name, (registryName) -> new SpiritBottleItem(block.get(), properties.setId(ResourceKey.create(Registries.ITEM, registryName))));
    }

    public static <T extends Block> DeferredItem<SpiritBottleItem> registerSpirit(String name, DeferredBlock<T> block, DeferredHolder<Fluid, FlowingFluid> fluid, Item.Properties properties) {
        return ITEM.register(name, (registryName) -> new SpiritBottleItem(block.get(), properties.setId(ResourceKey.create(Registries.ITEM, registryName))
                .component(DataComponentTypeRegistries.SPIRIT_CONTENT, new FluidStackDataComponent(new FluidStack(fluid, 1000)))));
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
