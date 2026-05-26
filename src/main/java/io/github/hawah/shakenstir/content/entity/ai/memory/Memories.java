package io.github.hawah.shakenstir.content.entity.ai.memory;

import com.mojang.serialization.Codec;
import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

public class Memories {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPE = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, ShakenStir.MODID);
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BoundingBox>> BAR_MEMORY = register("bar_memory", BoundingBox.CODEC);


    public static void register(IEventBus modEventBus) {
        MEMORY_MODULE_TYPE.register(modEventBus);
    }

    private static <U> DeferredHolder<MemoryModuleType<?>, MemoryModuleType<U>> register(String name, Codec<U> codec) {
        return MEMORY_MODULE_TYPE.register(name, () -> new MemoryModuleType<>(Optional.of(codec)));
    }
}
