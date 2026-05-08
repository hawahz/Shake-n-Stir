package io.github.hawah.shakenstir.content.block;

import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import io.github.hawah.shakenstir.content.recipe.Spirits;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockRegistries {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ShakenStir.MODID);
    public static final DeferredBlock<Shake> SHAKE_BLOCK = register("shake", Shake::new);
    public static final DeferredBlock<ShakeCup> SHAKE_CUP_BLOCK = register("shake_cup", ShakeCup::new);
    public static final DeferredBlock<CenteredSpiritBlock> GIN = register("gin", CenteredSpiritBlock::new, Spirits.GIN);
    public static final DeferredBlock<LiquidBlock> GIN_LIQUID = registerLiquid("gin_liquid", FluidRegistries.GIN_SOURCE_FLUID_BLOCK);
    public static final DeferredBlock<LiquidBlock> VODKA_LIQUID = registerLiquid("vodka_liquid", FluidRegistries.VODKA_FLOWING_FLUID_BLOCK);
    public static final DeferredBlock<LiquidBlock> WHISKY_LIQUID = registerLiquid("whisky_liquid", FluidRegistries.WHISKY_FLOWING_FLUID_BLOCK);
    public static final DeferredBlock<LiquidBlock> RUM_LIQUID = registerLiquid("rum_liquid", FluidRegistries.RUM_FLOWING_FLUID_BLOCK);
    public static final DeferredBlock<LiquidBlock> TEQUILA_LIQUID = registerLiquid("tequila_liquid", FluidRegistries.TEQUILA_FLOWING_FLUID_BLOCK);
    public static final DeferredBlock<LiquidBlock> BRANDY_LIQUID = registerLiquid("brandy_liquid", FluidRegistries.BRANDY_FLOWING_FLUID_BLOCK);

    public static final DeferredRegister<MapCodec<? extends Block>> REGISTRAR = DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, ShakenStir.MODID);
    public static final Supplier<MapCodec<Shake>> SHAKE_CODEC = REGISTRAR.register(
            "simple",
            () -> BlockBehaviour.simpleCodec(Shake::new)
    );
    public static final Supplier<MapCodec<CenteredSpiritBlock>> CENTERED_SPIRIT_CODEC = REGISTRAR.register(
            "centered_spirit",
            () -> BlockBehaviour.simpleCodec(CenteredSpiritBlock::new)
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
    public static <T extends Block> DeferredBlock<T> register(String name, BiFunction<BlockBehaviour.Properties, Spirits, T> blockSupplier, Spirits spirits) {
        return BLOCKS.register(name, registryName ->
                blockSupplier.apply(
                        BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, registryName)),
                        spirits
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

}
