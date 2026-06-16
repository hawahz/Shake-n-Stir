package io.github.hawah.shakenstir.content.item;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.MintSizeComponent;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


@SuppressWarnings("unused")
public class SnsCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, ShakenStir.MODID);
    //CREATIVE_MODE_TABS is a DeferredRegister<CreativeModeTab>
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SHAKENSTIR_TAB_BARTENDING = CREATIVE_MODE_TABS.register("shakenstir_bartending_tab", () -> CreativeModeTab.builder()
            //Set the title of the tab. Don't forget to add a translation!
            .title(Component.translatable("itemGroup." + ShakenStir.MODID + ".tab.bartending"))
            //Set the icon of the tab.
            .icon(() -> new ItemStack(ItemRegistries.SHAKER.get()))
            //Add your items to the tab.
            .displayItems((params, output) -> {
                output.accept(ItemRegistries.SHAKER.get());
                output.accept(ItemRegistries.SHAKER_LID.get());
                output.accept(ItemRegistries.ICE_CUBE.get());
                output.accept(ItemRegistries.BOTTLE.get());
                output.accept(ItemRegistries.GIN.get());
                output.accept(ItemRegistries.WHISKY.get());
                output.accept(ItemRegistries.VODKA.get());
                output.accept(ItemRegistries.RUM.get());
                output.accept(ItemRegistries.TEQUILA.get());
                output.accept(ItemRegistries.BRANDY.get());
                output.accept(ItemRegistries.BUBBLE.get());
                output.accept(ItemRegistries.TONIC.get());
                output.accept(ItemRegistries.BITTERS.get());
                output.accept(createLongDrink("collins_glass"));
                output.accept(createShortDrink("martini_glass"));
                output.accept(createShortDrink("margarita_glass"));
                output.accept(ItemRegistries.LEMON.get());
                output.accept(ItemRegistries.LEMON_SLICE.get());
                output.accept(ItemRegistries.SOBERING_TEA.get());
            })
            .build()
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SHAKENSTIR_TAB_BAR = CREATIVE_MODE_TABS.register("shakenstir_tab_bar", () -> CreativeModeTab.builder()
            //Set the title of the tab. Don't forget to add a translation!
            .title(Component.translatable("itemGroup." + ShakenStir.MODID + ".tab.bar"))
            //Set the icon of the tab.
            .icon(() -> new ItemStack(ItemRegistries.CABINET.get()))
            //Add your items to the tab.
            .displayItems((params, output) -> {
                output.accept(ItemRegistries.BAR_COUNTER.get());
                output.accept(ItemRegistries.CABINET.get());
                output.accept(ItemRegistries.DISTILLER.get());
                output.accept(ItemRegistries.MENU.get());
                output.accept(ItemRegistries.RAG.get());
                output.accept(ItemRegistries.RECIPE_SCROLL.get());
                output.accept(ItemRegistries.BARTENDER_SPAWNER);
                output.accept(ItemRegistries.BARTENDER_GLOVE);
                output.accept(ItemRegistries.DIALOGUE_EDITOR);
            })
            .build()
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SHAKENSTIR_TAB_DECORATION = CREATIVE_MODE_TABS.register("shakenstir_tab_decoration", () -> CreativeModeTab.builder()
            //Set the title of the tab. Don't forget to add a translation!
            .title(Component.translatable("itemGroup." + ShakenStir.MODID + ".tab.decoration"))
            //Set the icon of the tab.
            .icon(() -> new ItemStack(Items.SUNFLOWER))
            //Add your items to the tab.
            .displayItems((params, output) -> {
                output.accept(Items.POPPY);
                output.accept(ItemRegistries.LEMON_SLICE.get());
                output.accept(ItemRegistries.MINT_SEED.get());
                output.accept(createMint(0));
                output.accept(createMint(1));
                output.accept(createMint(2));

                output.accept(ItemRegistries.LEMON_SAPLING);
                output.accept(ItemRegistries.LEMON_LOG);
                output.accept(ItemRegistries.LEMON_SIDE_LEAVES);
                output.accept(ItemRegistries.LEMON_TOP_LEAVES);
                output.accept(ItemRegistries.LEMON_LEAVES);
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

    public static ItemStack createMint(int size) {
        ItemStack stack = ItemRegistries.MINT.toStack();
        stack.set(DataComponentTypeRegistries.MINT_SIZE, MintSizeComponent.of(size));
        return stack;
    }

    public static void register(IEventBus event) {
        CREATIVE_MODE_TABS.register(event);
    }
}
