package io.github.hawah.shakenstir.content.item;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
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
                output.accept(createLongDrink("collins_glass"));
                output.accept(createShortDrink("martini_glass"));
                output.accept(createShortDrink("margarita_glass"));
            })
            .build()
    );

    public static ItemStack createLongDrink(String path) {
        ItemStack stack = ItemRegistries.LONG_DRINK_GLASSWARE.toStack();
        stack.set(DataComponents.ITEM_MODEL, ShakenStir.asResource(path));
        stack.set(DataComponents.ITEM_NAME, LangData.getFromItem(path));
        stack.set(DataComponentTypeRegistries.GLASSWARE_NAME, LangData.getFromItem(path));
        return stack;
    }

    public static ItemStack createShortDrink(String path) {
        ItemStack stack = ItemRegistries.SHORT_DRINK_GLASSWARE.toStack();
        stack.set(DataComponents.ITEM_MODEL, ShakenStir.asResource(path));
        stack.set(DataComponents.ITEM_NAME, LangData.getFromItem(path));
        stack.set(DataComponentTypeRegistries.GLASSWARE_NAME, LangData.getFromItem(path));
        return stack;
    }

    public static void register(IEventBus event) {
        CREATIVE_MODE_TABS.register(event);
    }
}
