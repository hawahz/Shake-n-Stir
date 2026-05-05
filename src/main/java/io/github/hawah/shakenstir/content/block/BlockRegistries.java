package io.github.hawah.shakenstir.content.block;

import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

public class BlockRegistries {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ShakenStir.MODID);
    public static final DeferredBlock<Shake> SHAKE_BLOCK = register("shake", Shake::new);
    public static final DeferredBlock<ShakeCup> SHAKE_CUP_BLOCK = register("shake_cup", ShakeCup::new);

    public static final DeferredRegister<MapCodec<? extends Block>> REGISTRAR = DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, ShakenStir.MODID);
    public static final Supplier<MapCodec<Shake>> SHAKE_CODEC = REGISTRAR.register(
            "simple",
            () -> BlockBehaviour.simpleCodec(Shake::new)
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
