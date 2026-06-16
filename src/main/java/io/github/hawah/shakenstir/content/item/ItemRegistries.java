package io.github.hawah.shakenstir.content.item;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.SpiritContent;
import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import io.github.hawah.shakenstir.foundation.item.DecorateItem;
import io.github.hawah.shakenstir.foundation.item.FluidHolderItem;
import io.github.hawah.shakenstir.foundation.item.PriorityBlockItem;
import io.github.hawah.shakenstir.foundation.item.SpiritBottleItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unused")
public class ItemRegistries {
    public static final DeferredRegister.Items ITEM = DeferredRegister.createItems(ShakenStir.MODID);
    public static final DeferredItem<PriorityBlockItem> SHAKER = register("shaker", ShakerItem::new);
    public static final DeferredItem<PriorityBlockItem> SHAKER_LID = register("shaker_lid", BlockRegistries.SHAKE_LID_BLOCK);
    public static final DeferredItem<Item> ICE_CUBE = register("ice_cube", Item::new);

    public static final DeferredItem<GlasswareItem> LONG_DRINK_GLASSWARE = registerGlass("long_drink_glassware", BlockRegistries.LONG_DRINK_GLASSWARE, new Item.Properties());
    public static final DeferredItem<GlasswareItem> SHORT_DRINK_GLASSWARE = registerGlass("short_drink_glassware", BlockRegistries.SHORT_DRINK_GLASSWARE, new Item.Properties());

    public static final DeferredItem<Item> CONTENT_HOLDER = register("shake_content_holder", Item::new);

    public static final DeferredItem<Item> LEMON = register("lemon", Item::new, new Item.Properties().food(Foods.APPLE).component(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), List.of(new MobEffectInstance(MobEffectRegistries.LEMON)), Optional.empty())));
    public static final DeferredItem<DecorateItem> LEMON_SLICE = register("lemon_slice", DecorateItem::new, new Item.Properties().component(DataComponentTypeRegistries.DECORATE_MODEL, ShakenStir.asResource("lemon_slice")));

    public static final DeferredItem<Rag> RAG = register("rag", Rag::new);
    public static final DeferredItem<MenuItem> MENU = register("menu", MenuItem::new);
    public static final DeferredItem<Item> DIALOGUE_EDITOR = register("dialogue_editor", Item::new, new Item.Properties().component(DataComponentTypeRegistries.DIALOGUE, Unit.INSTANCE));

    // Spirit
    public static final DeferredItem<SpiritBottleItem> GIN = registerSpirit("gin", BlockRegistries.GIN, new Item.Properties().component(DataComponentTypeRegistries.SPIRIT_CONTENT, new SpiritContent(new FluidStack(FluidRegistries.GIN_SOURCE, 1000))));
    public static final DeferredItem<SpiritBottleItem> WHISKY = registerSpirit("whisky", BlockRegistries.WHISKY, new Item.Properties().component(DataComponentTypeRegistries.SPIRIT_CONTENT, new SpiritContent(new FluidStack(FluidRegistries.WHISKY_SOURCE, 1000))));
    public static final DeferredItem<SpiritBottleItem> VODKA = registerSpirit("vodka", BlockRegistries.VODKA, new Item.Properties().component(DataComponentTypeRegistries.SPIRIT_CONTENT, new SpiritContent(new FluidStack(FluidRegistries.VODKA_SOURCE, 1000))));
    public static final DeferredItem<SpiritBottleItem> RUM = registerSpirit("rum", BlockRegistries.RUM, new Item.Properties().component(DataComponentTypeRegistries.SPIRIT_CONTENT, new SpiritContent(new FluidStack(FluidRegistries.RUM_SOURCE, 1000))));
    public static final DeferredItem<SpiritBottleItem> TEQUILA = registerSpirit("tequila", BlockRegistries.TEQUILA, new Item.Properties().component(DataComponentTypeRegistries.SPIRIT_CONTENT, new SpiritContent(new FluidStack(FluidRegistries.TEQUILA_SOURCE, 1000))));
    public static final DeferredItem<SpiritBottleItem> BRANDY = registerSpirit("brandy", BlockRegistries.BRANDY, new Item.Properties().component(DataComponentTypeRegistries.SPIRIT_CONTENT, new SpiritContent(new FluidStack(FluidRegistries.BRANDY_SOURCE, 1000))));

    public static final DeferredItem<SpiritBottleItem> BOTTLE = registerSpirit("bottle", BlockRegistries.BOTTLE, new Item.Properties().component(DataComponentTypeRegistries.SPIRIT_CONTENT, new SpiritContent(FluidStack.EMPTY)));

    public static final DeferredItem<FluidHolderItem> BUBBLE = registerFluidHolder("bubbles", FluidRegistries.BUBBLE_SOURCE);
    public static final DeferredItem<FluidHolderItem> TONIC = registerFluidHolder("tonic", FluidRegistries.TONIC_SOURCE);
    public static final DeferredItem<FluidHolderItem> BITTERS = registerFluidHolder("bitters", FluidRegistries.BITTERS_SOURCE);

    public static final DeferredItem<SoberingTea> SOBERING_TEA = register("sobering_tea", SoberingTea::new);

    public static final DeferredItem<PriorityBlockItem> CABINET = register("cabinet", BlockRegistries.CABINET);
    public static final DeferredItem<PriorityBlockItem> DISTILLER = register("distiller", BlockRegistries.DISTILLER);
    public static final DeferredItem<PriorityBlockItem> BAR_COUNTER = register("bar_counter", BlockRegistries.BAR_COUNTER_BLOCK);
    public static final DeferredItem<RecipeScroll> RECIPE_SCROLL = register("recipe_scroll", RecipeScroll::new);
    public static final DeferredItem<BartenderSpawner> BARTENDER_SPAWNER = register("bartender_spawner", BartenderSpawner::new);

    public static final DeferredItem<Item> BARTENDER_GLOVE = register("bartender_glove", Item::new, new Item.Properties().durability(20).component(DataComponentTypeRegistries.BARTENDER_GLOVE, Unit.INSTANCE));
    public static final DeferredItem<MintItem> MINT = registerMint("mint", 0);
    public static final DeferredItem<StackedMintItem> STACKED_MINT = register("stacked_mint", StackedMintItem::new);
    public static final DeferredItem<PriorityBlockItem> MINT_SEED = register("mint_seed", BlockRegistries.MINT_PLANT);

    public static <T extends Item> DeferredItem<T> register(String name, Function<Item.Properties, T> supply) {
        return ITEM.register(name, (registryName) -> supply.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))));
    }

    public static DeferredItem<MintItem> registerMint(String name, int idx) {
        return ITEM.register(name, (registryName) -> new MintItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName)), idx));
    }

    public static <T extends Item> DeferredItem<T> register(String name, Function<Item.Properties, T> supply, Item.Properties properties) {
        return ITEM.register(name, (registryName) -> supply.apply(properties.setId(ResourceKey.create(Registries.ITEM, registryName))));
    }
    public static <T extends Block> DeferredItem<PriorityBlockItem> register(String name, DeferredBlock<T> block) {
        return ITEM.register(name, (registryName) -> new PriorityBlockItem(block.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))));
    }

    public static <T extends Block> DeferredItem<SpiritBottleItem> registerSpirit(String name, DeferredBlock<T> block, Item.Properties properties) {
        return ITEM.register(name, (registryName) -> new SpiritBottleItem(block.get(), properties.setId(ResourceKey.create(Registries.ITEM, registryName))));
    }

    public static DeferredItem<FluidHolderItem> registerFluidHolder(String name, DeferredHolder<Fluid, FlowingFluid> fluid, int amount) {
        Item.Properties component = new Item.Properties()
                .component(DataComponentTypeRegistries.SPIRIT_CONTENT, new SpiritContent(new FluidStack(fluid, amount)));
        return ITEM.register(name, (registryName) ->
                new FluidHolderItem(component.setId(ResourceKey.create(Registries.ITEM, registryName))));
    }

    public static DeferredItem<FluidHolderItem> registerFluidHolder(String name, DeferredHolder<Fluid, FlowingFluid> fluid) {
        return registerFluidHolder(name, fluid, 1000);
    }

    public static <T extends Block> DeferredItem<GlasswareItem> registerGlass(String name, DeferredBlock<T> block, Item.Properties properties) {
        return ITEM.register(name, (registryName) -> new GlasswareItem(block.get(), properties.setId(ResourceKey.create(Registries.ITEM, registryName))));
    }

    public static <T extends Block> DeferredItem<SpiritBottleItem> registerSpirit(String name, DeferredBlock<T> block, DeferredHolder<Fluid, FlowingFluid> fluid, Item.Properties properties) {
        return ITEM.register(name, (registryName) -> new SpiritBottleItem(block.get(), properties.setId(ResourceKey.create(Registries.ITEM, registryName))
                .component(DataComponentTypeRegistries.SPIRIT_CONTENT, new SpiritContent(new FluidStack(fluid, 1000)))));
    }

    public static void register(IEventBus eventBus) {
        ITEM.register(eventBus);
    }

//    @SubscribeEvent
//    public static void registerCapability(RegisterCapabilitiesEvent event) {
//        event.registerItem(
//                Capabilities.Item.ITEM,
//                (itemStack, itemAccess) -> new ShakeContentHolder(itemAccess, DataComponentTypeRegistries.SHAKE_ITEM_INGREDIENT, 6),
//                ItemRegistries.SHAKE
//        );
//    }
}
