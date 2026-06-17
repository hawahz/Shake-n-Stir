package io.github.hawah.shakenstir.content.block;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("SameParameterValue")
@EventBusSubscriber
public class BlockRegistries {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ShakenStir.MODID);
    public static final DeferredBlock<Shaker> SHAKE_BLOCK = register("shaker", Shaker::new);
    public static final DeferredBlock<ShakerLid> SHAKE_LID_BLOCK = register("shaker_lid", ShakerLid::new);

    public static final DeferredBlock<SpiritBlock> BUBBLE = register("bubble", SpiritBlock::new);
    public static final DeferredBlock<SpiritBlock> GIN = register("gin", SpiritBlock::new);
    public static final DeferredBlock<SpiritBlock> VODKA = register("vodka", SpiritBlock::new);
    public static final DeferredBlock<SpiritBlock> WHISKY = register("whisky", SpiritBlock::new);
    public static final DeferredBlock<SpiritBlock> RUM = register("rum", SpiritBlock::new);
    public static final DeferredBlock<SpiritBlock> TEQUILA = register("tequila", SpiritBlock::new);
    public static final DeferredBlock<SpiritBlock> BRANDY = register("brandy", SpiritBlock::new);
    public static final DeferredBlock<SpiritBlock> BOTTLE = register("bottle", SpiritBlock::new);
    public static final DeferredBlock<Glassware> LONG_DRINK_GLASSWARE = register("long_drink_glassware", Glassware::new);
    public static final DeferredBlock<Glassware> SHORT_DRINK_GLASSWARE = register("short_drink_glassware", Glassware::new);
    public static final DeferredBlock<MintPlantBlock> MINT_PLANT = register("mint_plant", MintPlantBlock::new);

    public static final DeferredBlock<Distiller> DISTILLER = register("distiller", Distiller::new);

    public static final DeferredBlock<Cabinet> CABINET = register("cabinet", Cabinet::new);
    public static final DeferredBlock<LemonSideLeavesBlock> LEMON_SIDE_LEAVES = register("lemon_side_leaves", LemonSideLeavesBlock::new);
    public static final DeferredBlock<LemonTreeBlock> LEMON_LOG = register("lemon_log", LemonTreeBlock::new, logProperties(MapColor.WOOD, SoundType.WOOD));
    public static final DeferredBlock<Block> LEMON_LEAVES = register("lemon_center_leaves", p -> new TintedParticleLeavesBlock(0.01F, p), leavesProperties(SoundType.GRASS));

    public static final DeferredBlock<Block> LEMON_TOP_LEAVES = register("lemon_top_leaves",p -> new TintedParticleLeavesBlock(0.01F, p), leavesProperties(SoundType.GRASS));
    public static final DeferredBlock<LemonTreeSaplingBlock> LEMON_SAPLING = register("lemon_sapling", LemonTreeSaplingBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final DeferredBlock<FlowerPotBlock> POTTED_LEMON = register("potted_lemon", properties -> new FlowerPotBlock(()-> (FlowerPotBlock) Blocks.FLOWER_POT, BlockRegistries.LEMON_SAPLING, properties), flowerPotProperties());

    public static final DeferredBlock<LiquidBlock> GIN_LIQUID = registerLiquid("gin_liquid", FluidRegistries.GIN_SOURCE);
    public static final DeferredBlock<LiquidBlock> VODKA_LIQUID = registerLiquid("vodka_liquid", FluidRegistries.VODKA_FLOWING);
    public static final DeferredBlock<LiquidBlock> WHISKY_LIQUID = registerLiquid("whisky_liquid", FluidRegistries.WHISKY_FLOWING);
    public static final DeferredBlock<LiquidBlock> RUM_LIQUID = registerLiquid("rum_liquid", FluidRegistries.RUM_FLOWING);
    public static final DeferredBlock<LiquidBlock> TEQUILA_LIQUID = registerLiquid("tequila_liquid", FluidRegistries.TEQUILA_FLOWING);
    public static final DeferredBlock<LiquidBlock> BRANDY_LIQUID = registerLiquid("brandy_liquid", FluidRegistries.BRANDY_FLOWING);
    public static final DeferredBlock<LiquidBlock> BUBBLE_LIQUID = registerLiquid("bubble_liquid", FluidRegistries.BRANDY_FLOWING);

    public static final DeferredBlock<BarMenuBlock> BAR_MENU_BLOCK = register("bar_menu", BarMenuBlock::new);
    public static final DeferredBlock<BarCounterBlock> BAR_COUNTER_BLOCK = register("bar_counter", BarCounterBlock::new);

    public static final DeferredBlock<RecipeScrollBlock> RECIPE_SCROLL_BLOCK = register("recipe_scroll", RecipeScrollBlock::new);


    public static final DeferredRegister<MapCodec<? extends Block>> REGISTRAR = DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, ShakenStir.MODID);
    public static final Supplier<MapCodec<Shaker>> SHAKE_CODEC = REGISTRAR.register(
            "shake",
            () -> BlockBehaviour.simpleCodec(Shaker::new)
    );
    public static final Supplier<MapCodec<SpiritBlock>> CENTERED_SPIRIT_CODEC = REGISTRAR.register(
            "centered_spirit",
            () -> BlockBehaviour.simpleCodec(SpiritBlock::new)
    );
    public static final Supplier<MapCodec<Cabinet>> CABINET_CODEC = REGISTRAR.register(
            "cabinet",
            () -> BlockBehaviour.simpleCodec(Cabinet::new)
    );
    public static final Supplier<MapCodec<BarMenuBlock>> BAR_MENU_CODEC = REGISTRAR.register(
            "bar_menu",
            () -> BlockBehaviour.simpleCodec(BarMenuBlock::new)
    );
    public static final Supplier<MapCodec<BarCounterBlock>> BAR_COUNTER_CODEC = REGISTRAR.register(
            "bar_counter",
            () -> BlockBehaviour.simpleCodec(BarCounterBlock::new)
    );

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        REGISTRAR.register(eventBus);
    }

    public static <T extends Block> DeferredBlock<T> register(String name, Function<BlockBehaviour.Properties, T> blockSupplier) {
        return BLOCKS.register(name, registryName ->
                blockSupplier.apply(
                        BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, registryName))
                )
        );
    }

    public static <T extends Block> DeferredBlock<T> register(String name, Function<BlockBehaviour.Properties, T> blockSupplier, BlockBehaviour.Properties properties) {
        return BLOCKS.register(name, registryName ->
                blockSupplier.apply(
                        properties.setId(ResourceKey.create(Registries.BLOCK, registryName))
                )
        );
    }
    public static <T extends Block> DeferredBlock<T> register(String name, BiFunction<BlockBehaviour.Properties, Supplier<FluidType>, T> blockSupplier, Supplier<FluidType> content) {
        return BLOCKS.register(name, registryName ->
                blockSupplier.apply(
                        BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, registryName)),
                        content
                )
        );
    }

    public static DeferredBlock<LiquidBlock> registerLiquid(String name, DeferredHolder<Fluid, FlowingFluid> fluid) {
        return register(name,
                p -> new LiquidBlock(fluid.get(), p.mapColor(MapColor.WATER)
                        .replaceable()
                        .noCollision()
                        .strength(100.0F)
                        .pushReaction(PushReaction.DESTROY)
                        .noLootTable()
                        .liquid()
                        .sound(SoundType.EMPTY)));
    }

    private static BlockBehaviour.Properties logProperties(MapColor topColor, SoundType soundType) {
        return BlockBehaviour.Properties.of()
                .mapColor(topColor)
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.0F)
                .sound(soundType)
                .ignitedByLava();
    }

    public static boolean never(BlockState state, BlockGetter blockGetter, BlockPos blockPos) {
        return false;
    }

    private static BlockBehaviour.Properties leavesProperties(SoundType soundType) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .strength(0.2F)
                .randomTicks()
                .sound(soundType)
                .noOcclusion()
                .isValidSpawn(Blocks::ocelotOrParrot)
                .isSuffocating(BlockRegistries::never)
                .isViewBlocking(BlockRegistries::never)
                .ignitedByLava()
                .pushReaction(PushReaction.DESTROY)
                .isRedstoneConductor(BlockRegistries::never);
    }

    public static BlockBehaviour.Properties flowerPotProperties() {
        return BlockBehaviour.Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY);
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LogUtils.getLogger().info("HELLO FROM COMMON SETUP");

        // 将柠檬树苗映射到对应的花盆方块，使原版花盆能识别
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(
                BuiltInRegistries.BLOCK.getKey(BlockRegistries.LEMON_SAPLING.get()),
                BlockRegistries.POTTED_LEMON
        );
    }

}
