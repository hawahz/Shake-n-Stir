package io.github.hawah.shakenstir.foundation.datapack.spirit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.content.recipe.Quality;
import io.github.hawah.shakenstir.foundation.datapack.EffectData;
import io.github.hawah.shakenstir.foundation.datapack.Registries;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 *
 * @param fluidType: 基酒对应的流体
 * @param effectData: 效果对应的数据，包含效果和对应的等级
 */
public record SpiritData(Holder<Fluid> fluidType, EffectData effectData) {
    public static final Codec<SpiritData> CODEC = RecordCodecBuilder.create( inst -> inst.group(
            FluidStack.FLUID_HOLDER_CODEC.fieldOf("fluid").forGetter(SpiritData::fluidType),
            EffectData.CODEC.fieldOf("positive").forGetter(SpiritData::effectData)
    ).apply(inst, SpiritData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpiritData> STREAM_CODEC = StreamCodec.composite(
            FluidStack.FLUID_HOLDER_STREAM_CODEC, SpiritData::fluidType,
            EffectData.STREAM_CODEC, SpiritData::effectData,
            SpiritData::new
    );

    public static SpiritData get(Level level, Holder<Fluid> fluidType) {
        return level.registryAccess()
                .lookup(Registries.SPIRIT_REGISTRY_KEY)
                .flatMap(registry ->
                        registry.stream()
                                .filter(spiritData -> spiritData.fluidType().equals(fluidType))
                                .findFirst()
                )
                .or(() -> Spirits.getBuiltIn(fluidType))
                .orElseThrow();
    }

    public MobEffectInstance get(Quality phase) {
        return effectData().get(phase);
    }
}
