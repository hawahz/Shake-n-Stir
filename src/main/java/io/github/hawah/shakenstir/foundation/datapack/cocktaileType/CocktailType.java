package io.github.hawah.shakenstir.foundation.datapack.cocktaileType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.foundation.datapack.EffectData;
import io.github.hawah.shakenstir.foundation.utils.ITranslatable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Comparator;
import java.util.List;

public record CocktailType(Identifier id, Identifier translationKey, List<EffectData> effects) implements ITranslatable {

    public static final Codec<CocktailType> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("id").forGetter(CocktailType::id),
            Identifier.CODEC.fieldOf("translation_key").forGetter(CocktailType::translationKey),
            EffectData.CODEC.listOf().fieldOf("effects").forGetter(CocktailType::effects)
    ).apply(inst, CocktailType::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CocktailType> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, CocktailType::id,
            Identifier.STREAM_CODEC, CocktailType::translationKey,
            EffectData.STREAM_CODEC.apply(ByteBufCodecs.list()), CocktailType::effects,
            CocktailType::new
    );

    public CocktailType(Identifier id, List<EffectData> effects) {
        this(id, id, effects);
    }

    public MutableComponent translate(List<FluidStack> fluidStacks, List<ItemStack> itemStacks) {
        MutableComponent translatable;
        if (!itemStacks.isEmpty() && !fluidStacks.isEmpty()){
            translatable = Component.translatable(
                    translationKey().toString(),
                    fluidStacks.stream().max(Comparator.comparingInt(FluidStack::getAmount)).orElseThrow().getHoverName(),
                    itemStacks.getFirst().getHoverName()
            );
        } else if (!itemStacks.isEmpty()) {
            translatable = Component.translatable(
                    translationKey().toString(),
                    "",
                    itemStacks.getFirst().getHoverName()
            );
        } else if (!fluidStacks.isEmpty()) {
            translatable = Component.translatable(
                    translationKey().toString(),
                    fluidStacks.stream().max(Comparator.comparingInt(FluidStack::getAmount)).orElseThrow().getHoverName(),
                    ""
            );
        } else {
            translatable = Component.translatable(translationKey().toString(), "", "");
        }
        return translatable;
    }

}
