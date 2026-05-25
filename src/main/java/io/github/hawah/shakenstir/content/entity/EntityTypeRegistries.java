package io.github.hawah.shakenstir.content.entity;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class EntityTypeRegistries {
    public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(ShakenStir.MODID);
    public static final Supplier<EntityType<BartenderEntity>> BARTENDER = ENTITY_TYPES
            .registerEntityType("bartender", BartenderEntity::new, MobCategory.MISC);

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
