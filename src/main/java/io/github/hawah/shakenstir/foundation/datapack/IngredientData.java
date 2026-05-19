package io.github.hawah.shakenstir.foundation.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public record IngredientData(Ingredient itemHolder, Holder<MobEffect> effectHolder, List<Integer> amplifiers) {
    public static final Codec<IngredientData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(IngredientData::itemHolder),
            MobEffect.CODEC.fieldOf("effect").forGetter(IngredientData::effectHolder),
            Codec.INT.listOf().fieldOf("amplifiers").forGetter(IngredientData::amplifiers)
    ).apply(inst, IngredientData::new));

    public static Optional<IngredientData> get(Level level, Holder<Item> itemHolder) {
        return level.registryAccess()
                .lookup(DatapackRegistries.INGREDIENT_REGISTRY_KEY)
                .flatMap(registry ->
                        registry.stream()
                                .filter(spiritData -> spiritData.itemHolder().equals(itemHolder))
                                .findFirst()
                );
    }
}
