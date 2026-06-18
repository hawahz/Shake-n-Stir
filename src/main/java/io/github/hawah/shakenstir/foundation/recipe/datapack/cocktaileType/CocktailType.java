package io.github.hawah.shakenstir.foundation.recipe.datapack.cocktaileType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.foundation.recipe.Quality;
import io.github.hawah.shakenstir.foundation.recipe.datapack.EffectData;
import io.github.hawah.shakenstir.foundation.tags.SnsFluidTags;
import io.github.hawah.shakenstir.foundation.utils.ITranslatable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 鸡尾酒基础效果只跟Quality相关
 * @param id: 鸡尾酒类型的ID
 * @param translationKey cocktail type 的翻译key
 * @param effects 该类型鸡尾酒的基础效果s
 */
public record CocktailType(Identifier id, Identifier translationKey, List<EffectData> effects, int alcohol) implements ITranslatable {

    public static final CocktailType EMPTY = new CocktailType(ShakenStir.asResource("empty"), List.of(), 0);

    public static final Codec<CocktailType> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("id").forGetter(CocktailType::id),
            Identifier.CODEC.fieldOf("translation_key").forGetter(CocktailType::translationKey),
            EffectData.CODEC.listOf().fieldOf("effects").forGetter(CocktailType::effects),
            Codec.INT.fieldOf("alcohol").forGetter(CocktailType::alcohol)
    ).apply(inst, CocktailType::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CocktailType> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, CocktailType::id,
            Identifier.STREAM_CODEC, CocktailType::translationKey,
            EffectData.STREAM_CODEC.apply(ByteBufCodecs.list()), CocktailType::effects,
            ByteBufCodecs.INT, CocktailType::alcohol,
            CocktailType::new
    );

    public CocktailType(Identifier id, List<EffectData> effects, int alcohol) {
        this(id, id.withPrefix("name."), effects, alcohol);
    }

    public List<MobEffectInstance> get(Quality quality) {
        int signedIndex = quality.toSignedIndex();
        if (signedIndex >= 0) {
            return effects.stream().map(effectData -> effectData.getPositive(signedIndex)).toList();
        } else {
            return effects.stream().map(effectData -> effectData.getNegative(-(signedIndex + 1))).toList();
        }
    }

    public MutableComponent translate(List<FluidStack> fluidStacks, List<ItemStack> itemStacks) {
        MutableComponent translatable;
        if (!itemStacks.isEmpty() && !fluidStacks.isEmpty()){
            translatable = Component.translatable(
                    translationKey().toString(),
                    fluidStacks.stream().max(Comparator.comparingInt(FluidStack::getAmount)).map(FluidStack::getHoverName).orElse(Component.empty()),
                    itemStacks.getFirst().getHoverName()
            );
        } else if (!itemStacks.isEmpty()) {
            translatable = Component.translatable(
                    translationKey().toString(),
                    "",
                    itemStacks.getFirst().getHoverName()
            );
        } else if (!fluidStacks.isEmpty()) {
            var stacks = new ArrayList<>(fluidStacks);
            stacks.removeIf(fluidStack -> fluidStack.is(SnsFluidTags.SPIRIT));
            if (stacks.isEmpty()){
                translatable = Component.translatable(
                        translationKey().toString(),
                        fluidStacks.stream().max(Comparator.comparingInt(FluidStack::getAmount)).map(FluidStack::getHoverName).orElse(Component.empty()),
                        ""
                );
            } else {
                translatable = Component.translatable(
                        translationKey().toString(),
                        fluidStacks.stream().max(Comparator.comparingInt(FluidStack::getAmount)).map(FluidStack::getHoverName).orElse(Component.empty()),
                        stacks.stream().max(Comparator.comparingInt(FluidStack::getAmount)).map(FluidStack::getHoverName).orElse(Component.empty())
                );
            }
        } else {
            translatable = Component.translatable(translationKey().toString(), "", "");
        }
        return translatable;
    }

}
