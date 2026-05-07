package io.github.hawah.shakenstir.content.block;

import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

public class BlockRegistries {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ShakenStir.MODID);
    public static final DeferredBlock<Shake> SHAKE_BLOCK = register("shake", Shake::new);
    public static final DeferredBlock<ShakeCup> SHAKE_CUP_BLOCK = register("shake_cup", ShakeCup::new);
    public static final DeferredBlock<CenteredSpiritBlock> GIN = register("gin", CenteredSpiritBlock::new);
    public static final DeferredBlock<LiquidBlock> GIN_LIQUID = register("gin_liquid",
            p -> new LiquidBlock(FluidRegistries.GIN_SOURCE_FLUID_BLOCK.get(), p.mapColor(MapColor.WATER)
                    .replaceable()
                    .noCollision()
                    .strength(100.0F)
                    .pushReaction(PushReaction.DESTROY)
                    .noLootTable()
                    .liquid()
                    .sound(SoundType.EMPTY)));

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

}
