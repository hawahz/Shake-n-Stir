package io.github.hawah.shakenstir.content.entity.ai.memory;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Memories {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPE = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, ShakenStir.MODID);


    public static void register(IEventBus modEventBus) {
        MEMORY_MODULE_TYPE.register(modEventBus);
    }
}
