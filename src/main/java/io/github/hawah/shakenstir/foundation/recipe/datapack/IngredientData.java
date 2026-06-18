package io.github.hawah.shakenstir.foundation.recipe.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.foundation.datapack.Registries;
import io.github.hawah.shakenstir.foundation.recipe.Quality;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import java.util.Optional;

public record IngredientData(Ingredient itemHolder, EffectData effect) {
    public static final Codec<IngredientData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(IngredientData::itemHolder),
            EffectData.CODEC.fieldOf("positive").forGetter(IngredientData::effect)
    ).apply(inst, IngredientData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, IngredientData> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, IngredientData::itemHolder,
            EffectData.STREAM_CODEC, IngredientData::effect,
            IngredientData::new
    );

    public static Optional<IngredientData> get(Level level, Holder<Item> itemHolder) {
        return level.registryAccess()
                .lookup(Registries.INGREDIENT_REGISTRY_KEY)
                .flatMap(registry ->
                        registry.stream()
                                .filter(spiritData -> spiritData.itemHolder().test(itemHolder.value().getDefaultInstance()))
                                .findFirst()
                );
    }

    public MobEffectInstance get(Quality phase) {
        return effect().get(phase);
    }
}
