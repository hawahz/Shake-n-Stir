package io.github.hawah.shakenstir.foundation.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.content.recipe.Quality;
import io.github.hawah.shakenstir.foundation.datapack.cocktaileType.CocktailType;
import io.github.hawah.shakenstir.foundation.datapack.spirit.SpiritData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public record DrinkData(
        CocktailType type,
        SpiritData base,
        List<SpiritData> extraSpirit,
        List<IngredientData> extraIngredients,
        Quality quality
) {
    public static final Codec<DrinkData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            CocktailType.CODEC.fieldOf("type").forGetter(DrinkData::type),
            SpiritData.CODEC.fieldOf("base").forGetter(DrinkData::base),
            SpiritData.CODEC.listOf().fieldOf("extra_spirits").forGetter(DrinkData::extraSpirit),
            IngredientData.CODEC.listOf().fieldOf("extra_ingredients").forGetter(DrinkData::extraIngredients),
            Quality.CODEC.fieldOf("quality").forGetter(DrinkData::quality)
    ).apply(inst, DrinkData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DrinkData> STREAM_CODEC = StreamCodec.composite(
            CocktailType.STREAM_CODEC, DrinkData::type,
            SpiritData.STREAM_CODEC, DrinkData::base,
            SpiritData.STREAM_CODEC.apply(ByteBufCodecs.list()), DrinkData::extraSpirit,
            IngredientData.STREAM_CODEC.apply(ByteBufCodecs.list()), DrinkData::extraIngredients,
            Quality.STREAM_CODEC, DrinkData::quality,
            DrinkData::new
    );

    public void apply(Level level, LivingEntity livingEntity) {
        List<MobEffectInstance> typeEnhance = type().get(quality());
        List<MobEffectInstance> mobEffectInstance = List.of(base().get(quality()));
        List<MobEffectInstance> ingredientEffect = extraIngredients().stream().map(IngredientData::effect).map(effectData -> effectData.get(quality())).toList();
        List<MobEffectInstance> finalEffects = new ArrayList<>(typeEnhance);
        finalEffects.addAll(mobEffectInstance);
        finalEffects.addAll(ingredientEffect);
        for (MobEffectInstance effect : finalEffects) {
            livingEntity.addEffect(effect);
        }
    }
}
