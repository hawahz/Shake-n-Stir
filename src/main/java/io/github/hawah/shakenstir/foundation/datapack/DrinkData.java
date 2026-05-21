package io.github.hawah.shakenstir.foundation.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import io.github.hawah.shakenstir.content.recipe.Quality;
import io.github.hawah.shakenstir.foundation.datapack.cocktaileType.CocktailType;
import io.github.hawah.shakenstir.foundation.datapack.spirit.SpiritData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public record DrinkData(
        CocktailType type,
        SpiritData base,
        List<SpiritData> extraSpirit,
        List<IngredientData> extraIngredients,
        Quality quality,
        int coldLevel
) {
    public static final Codec<DrinkData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            CocktailType.CODEC.fieldOf("type").forGetter(DrinkData::type),
            SpiritData.CODEC.fieldOf("base").forGetter(DrinkData::base),
            SpiritData.CODEC.listOf().fieldOf("extra_spirits").forGetter(DrinkData::extraSpirit),
            IngredientData.CODEC.listOf().fieldOf("extra_ingredients").forGetter(DrinkData::extraIngredients),
            Quality.CODEC.fieldOf("quality").forGetter(DrinkData::quality),
            Codec.INT.fieldOf("cold_level").forGetter(DrinkData::coldLevel)
    ).apply(inst, DrinkData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DrinkData> STREAM_CODEC = StreamCodec.composite(
            CocktailType.STREAM_CODEC, DrinkData::type,
            SpiritData.STREAM_CODEC, DrinkData::base,
            SpiritData.STREAM_CODEC.apply(ByteBufCodecs.list()), DrinkData::extraSpirit,
            IngredientData.STREAM_CODEC.apply(ByteBufCodecs.list()), DrinkData::extraIngredients,
            Quality.STREAM_CODEC, DrinkData::quality,
            ByteBufCodecs.INT, DrinkData::coldLevel,
            DrinkData::new
    );

    public static final int[] COLD_LEVELS = new int[]{2 * 10, 8 * 10, 18 * 10};

    public void apply(LivingEntity livingEntity) {
        List<MobEffectInstance> typeEnhance = type().get(quality());
        List<MobEffectInstance> mobEffectInstance = List.of(base().get(quality()));
        List<MobEffectInstance> ingredientEffect = extraIngredients().stream().map(IngredientData::effect).map(effectData -> effectData.get(quality())).toList();
        List<MobEffectInstance> finalEffects = new ArrayList<>(typeEnhance);
        finalEffects.addAll(mobEffectInstance);
        finalEffects.addAll(ingredientEffect);
        livingEntity.addEffect(
                new MobEffectInstance(
                        MobEffectRegistries.PARALYSIS,
                        // TODO 将ColdLevels的计算逻辑变成线性而非离散数组
                        COLD_LEVELS[Mth.clamp(coldLevel() - 1, 0, COLD_LEVELS.length - 1)]
        ));
        for (MobEffectInstance effect : finalEffects) {
            livingEntity.addEffect(effect);
        }
        int drunkAmplifier = extraSpirit().size();
        MobEffectInstance instance = new MobEffectInstance(MobEffectRegistries.DRUNK, 20 * 60 * 5, drunkAmplifier);
        livingEntity.addEffect(instance);
    }

    public List<MobEffectInstance> cocktailEffects() {
        return type().get(quality());
    }

    public List<MobEffectInstance> baseEffects() {
        return List.of(base().get(quality()));
    }

    public List<MobEffectInstance> ingredientEffects() {
        return extraIngredients().stream().map(IngredientData::effect).map(effectData -> effectData.get(quality())).toList();
    }

    public List<MobEffectInstance> allEffects() {
        List<MobEffectInstance> effects = new ArrayList<>(cocktailEffects());
        effects.addAll(baseEffects());
        effects.addAll(ingredientEffects());
        return effects;
    }

    public List<MobEffectInstance> coldEffects() {
        return List.of(new MobEffectInstance(MobEffectRegistries.PARALYSIS, COLD_LEVELS[Mth.clamp(coldLevel() - 1, 0, COLD_LEVELS.length - 1)]));
    }

    public List<MobEffectInstance> drunkEffects() {
        return List.of(new MobEffectInstance(MobEffectRegistries.DRUNK, 20 * 60 * 5));
    }

    public int drunkLevel() {
        return (int) ((extraSpirit().size() + 1)/4F * 45);
    }
}
