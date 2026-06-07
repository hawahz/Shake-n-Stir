package io.github.hawah.shakenstir.content.recipe;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.recipe.shake.ShakeRecipe;
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
    public static final Supplier<RecipeType<StirRecipe>> STIR_RECIPE =
            RECIPE_TYPES.register("stir_recipe", RecipeType::simple);
    public static final Supplier<RecipeType<DistillerRecipe>> DISTILLER_RECIPE =
            RECIPE_TYPES.register("distiller_recipe", RecipeType::simple);
    public static final Supplier<RecipeType<SpiritBottleSpecialRecipe>> SPIRIT_BOTTLE_SPECIAL_RECIPE =
            RECIPE_TYPES.register("spirit_bottle_special_recipe", RecipeType::simple);


    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ShakenStir.MODID);
    public static final Supplier<RecipeSerializer<ShakeRecipe>> SHAKE_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("shake_recipe", () -> new RecipeSerializer<>(ShakeRecipe.CODEC, ShakeRecipe.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<StirRecipe>> STIR_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("stir_recipe", () -> new RecipeSerializer<>(StirRecipe.CODEC, StirRecipe.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<DistillerRecipe>> DISTILLER_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("distiller_recipe", () -> new RecipeSerializer<>(DistillerRecipe.CODEC, DistillerRecipe.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<SpiritBottleSpecialRecipe>> SPIRIT_BOTTLE_SPECIAL_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("spirit_bottle_special_recipe", () -> SpiritBottleSpecialRecipe.SERIALIZER);

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
