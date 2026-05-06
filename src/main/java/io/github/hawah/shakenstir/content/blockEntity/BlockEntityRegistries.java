package io.github.hawah.shakenstir.content.blockEntity;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockEntityRegistries {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ShakenStir.MODID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShakeBlockEntity>> SHAKE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("shake_be", () -> new BlockEntityType<>(ShakeBlockEntity::new, false, BlockRegistries.SHAKE_BLOCK.get()));
    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
