package io.github.hawah.shakenstir.content.item;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SnsCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, ShakenStir.MODID);
    //CREATIVE_MODE_TABS is a DeferredRegister<CreativeModeTab>
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SHAKENSTIR_TAB = CREATIVE_MODE_TABS.register("shakenstir_tab", () -> CreativeModeTab.builder()
            //Set the title of the tab. Don't forget to add a translation!
            .title(Component.translatable("itemGroup." + ShakenStir.MODID + ".tab"))
            //Set the icon of the tab.
            .icon(() -> new ItemStack(ItemRegistries.SHAKE.get()))
            //Add your items to the tab.
            .displayItems((params, output) -> {
                output.accept(ItemRegistries.SHAKE.get());
                output.accept(ItemRegistries.SHAKE_CUP.get());
                output.accept(ItemRegistries.ICE_CUBE.get());
                output.accept(ItemRegistries.GIN.get());
                output.accept(ItemRegistries.WHISKY.get());
            })
            .build()
    );

    public static void register(IEventBus event) {
        CREATIVE_MODE_TABS.register(event);
    }
}
