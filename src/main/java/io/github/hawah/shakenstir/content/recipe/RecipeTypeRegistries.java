package io.github.hawah.shakenstir.content.recipe;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RecipeTypeRegistries {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, ShakenStir.MODID);

    public static final Supplier<RecipeType<ShakeRecipe>> SHAKE_RECIPE =
            RECIPE_TYPES.register("shake_recipe", RecipeType::simple);


    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ShakenStir.MODID);
    public static final Supplier<RecipeSerializer<ShakeRecipe>> SHAKE_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("shake_recipe", () -> new RecipeSerializer<>(ShakeRecipe.CODEC, ShakeRecipe.STREAM_CODEC));

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
