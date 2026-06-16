package io.github.hawah.shakenstir.content.entity.ai.memory;


import com.mojang.serialization.Codec;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.foundation.data.SnsRecipeHolder;
import io.github.hawah.shakenstir.foundation.data.SnsRecipeStack;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Optional;

public class Memories {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPE = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, ShakenStir.MODID);
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<MemoryEntitiesHolder<LivingEntity>>> IGNORED_ENTITIES = register("ignored_entities");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<GlobalPos>> MENU = register("menu", GlobalPos.CODEC);
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Integer>> IDLE_TIME = register("idle_time", Codec.INT);
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> IDLING = register("idling", Unit.CODEC);
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> RECIPE_READY = register("recipe_ready", Unit.CODEC);
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<SnsRecipeHolder>> RECIPE = register("recipe", SnsRecipeHolder.CODEC);
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<List<SnsRecipeStack>>> RECIPES_TODO = register("recipes_todo", SnsRecipeStack.CODEC.listOf());
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<MemoryEntitiesHolder<Player>>> UNSERVED_CUSTOMER = register("unserved_customer");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BarData>> BAR_MEMORY = register("bar_memory", BarData.CODEC);
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<List<Ingredient>>> ITEM_TO_FIND = register("item_to_find", Ingredient.CODEC.listOf());
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<GlobalPos>> MEMORY_GLASSWARE = register("memory_glassware", GlobalPos.CODEC);


    public static void register(IEventBus modEventBus) {
        MEMORY_MODULE_TYPE.register(modEventBus);
    }

    private static <U> DeferredHolder<MemoryModuleType<?>, MemoryModuleType<U>> register(String name, Codec<U> codec) {
        return MEMORY_MODULE_TYPE.register(name, () -> new MemoryModuleType<>(Optional.of(codec)));
    }

    private static <U> DeferredHolder<MemoryModuleType<?>, MemoryModuleType<U>> register(String name) {
        return MEMORY_MODULE_TYPE.register(name, () -> new MemoryModuleType<>(Optional.empty()));
    }
}
