package io.github.hawah.shakenstir.foundation.recipe.datapack;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.recipe.Quality;
import io.github.hawah.shakenstir.foundation.recipe.datapack.cocktaileType.CocktailType;
import io.github.hawah.shakenstir.foundation.recipe.datapack.spirit.SpiritData;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import io.github.hawah.shakenstir.foundation.tags.SnsFluidTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.TooltipProvider;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@SuppressWarnings("unused")
public record DrinkData(
        CocktailType type,
        SpiritData base,
        List<SpiritData> extraSpirit,
        List<IngredientData> extraIngredients,
        Quality quality,
        List<Consumable> consumables,
        int coldLevel
) implements TooltipProvider {
    public static final Codec<DrinkData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            CocktailType.CODEC.fieldOf("type").forGetter(DrinkData::type),
            SpiritData.CODEC.fieldOf("base").forGetter(DrinkData::base),
            SpiritData.CODEC.listOf().fieldOf("extra_spirits").forGetter(DrinkData::extraSpirit),
            IngredientData.CODEC.listOf().fieldOf("extra_ingredients").forGetter(DrinkData::extraIngredients),
            Quality.CODEC.fieldOf("quality").forGetter(DrinkData::quality),
            Consumable.CODEC.listOf().optionalFieldOf("consumables", List.of()).forGetter(DrinkData::consumables),
            Codec.INT.fieldOf("cold_level").forGetter(DrinkData::coldLevel)
    ).apply(inst, DrinkData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DrinkData> STREAM_CODEC = StreamCodec.composite(
            CocktailType.STREAM_CODEC, DrinkData::type,
            SpiritData.STREAM_CODEC, DrinkData::base,
            SpiritData.STREAM_CODEC.apply(ByteBufCodecs.list()), DrinkData::extraSpirit,
            IngredientData.STREAM_CODEC.apply(ByteBufCodecs.list()), DrinkData::extraIngredients,
            Quality.STREAM_CODEC, DrinkData::quality,
            Consumable.STREAM_CODEC.apply(ByteBufCodecs.list()), DrinkData::consumables,
            ByteBufCodecs.INT, DrinkData::coldLevel,
            DrinkData::new
    );

    @SuppressWarnings("unused")
    public DrinkData(CocktailType type, SpiritData base, List<SpiritData> extraSpirit, List<IngredientData> extraIngredients, Quality quality, int coldLevel) {
        this(type, base, extraSpirit, extraIngredients, quality, List.of(), coldLevel);
    }

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
        //noinspection resource
        if (!livingEntity.level().isClientSide()){
            consumables.forEach(
                    consumable ->
                            consumable.onConsumeEffects().forEach(effect -> effect.apply(livingEntity.level(), ItemRegistries.CONTENT_HOLDER.toStack(), livingEntity))
            );
        }
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
        return List.of(new MobEffectInstance(MobEffectRegistries.DRUNK, 20 * 60 * 5, drunkLevel()/15));
    }

    public int drunkLevel() {
        int alcohol = type.alcohol();
        if (extraSpirit.isEmpty()) {
            return alcohol;
        }
        int spiritAmount = Math.toIntExact(extraSpirit.stream()
                .filter(spiritData -> spiritData.fluidType().is(SnsFluidTags.SPIRIT))
                .count());
        float addition = spiritAmount / (float) (extraSpirit.size()) * 40;

        return (int) (alcohol * 4 + addition * extraSpirit.size())/ (4 + extraSpirit.size());
    }

    @Override
    public void addToTooltip(Item.TooltipContext ctx, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        startPotion();
        boolean moreInformation = flag.hasShiftDown();
        MutableComponent translate = type().translate(List.of(), List.of());
        String string = translate.getString().replaceAll("^\\s+|\\s+", " ").trim();
        consumer.accept(LangData.TOOLTIP_TITLE_COCKTAIL.get(string));
        Quality quality = quality();
        if (moreInformation) {
            List<MobEffectInstance> cocktailEffects = type().effects().stream().map(effectData -> effectData.get(quality)).toList();
            if (!cocktailEffects.isEmpty()) {
                PotionContents.addPotionTooltip(
                        cocktailEffects,
                        consumer,
                        1.0F,
                        ctx.tickRate()
                );
            }
        }
        FluidStack fluidStack = new FluidStack(base().fluidType().value(), 250);
        consumer.accept(LangData.TOOLTIP_TITLE_BASE.get(fluidStack.getHoverName()));
        if (moreInformation) {
            addPotionTooltip(
                    baseEffects(),
                    consumer,
                    1.0F,
                    ctx.tickRate()
            );
        }
        consumer.accept(LangData.TOOLTIP_TITLE_DRUNK_LEVEL.get(drunkLevel()));
        if (moreInformation) {
            addPotionTooltip(
                    drunkEffects(),
                    consumer,
                    1.0F,
                    ctx.tickRate()
            );
        }
        consumer.accept(LangData.TOOLTIP_TITLE_ICE_LEVEL.get(coldLevel()));
        if (moreInformation) {
            addPotionTooltip(
                    coldEffects(),
                    consumer,
                    1.0F,
                    ctx.tickRate()
            );
        }
        if (!moreInformation) {
            consumer.accept(LangData.SHIFT.get());
        }
        endPotion(consumer);
    }

    public static List<Pair<Holder<Attribute>, AttributeModifier>> modifiers = Lists.newArrayList();

    public void startPotion() {
        modifiers.clear();
    }
    public void endPotion(Consumer<Component> lines) {
        if (!modifiers.isEmpty()) {
            lines.accept(CommonComponents.EMPTY);
            lines.accept(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

            // Neo: Override handling of potion attribute tooltips to support IAttributeExtension
            net.neoforged.neoforge.common.util.AttributeUtil.addPotionTooltip(modifiers, lines);
        }
        modifiers.clear();
    }
    public void addPotionTooltip(Iterable<MobEffectInstance> effects, Consumer<Component> lines, float durationScale, float tickrate) {

        for (MobEffectInstance effect : effects) {
            Holder<MobEffect> mobEffect = effect.getEffect();
            int amplifier = effect.getAmplifier();
            mobEffect.value().createModifiers(amplifier, (attribute, modifierx) -> modifiers.add(new Pair<>(attribute, modifierx)));
            MutableComponent line = PotionContents.getPotionDescription(mobEffect, amplifier);
            if (!effect.endsWithin(20)) {
                line = Component.translatable("potion.withDuration", line, MobEffectUtil.formatDuration(effect, durationScale, tickrate));
            }

            lines.accept(line.withStyle(mobEffect.value().getCategory().getTooltipFormatting()));
        }
    }
}
