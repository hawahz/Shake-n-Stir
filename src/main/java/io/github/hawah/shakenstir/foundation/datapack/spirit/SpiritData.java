package io.github.hawah.shakenstir.foundation.datapack.spirit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.foundation.datapack.DatapackRegistries;
import io.github.hawah.shakenstir.foundation.datapack.EffectData;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public record SpiritData(Holder<Fluid> fluidType, EffectData effectData) {
    public static final Codec<SpiritData> CODEC = RecordCodecBuilder.create( inst -> inst.group(
            FluidStack.FLUID_HOLDER_CODEC.fieldOf("fluid").forGetter(SpiritData::fluidType),
            EffectData.CODEC.fieldOf("effect").forGetter(SpiritData::effectData)
    ).apply(inst, SpiritData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpiritData> STREAM_CODEC = StreamCodec.composite(
            FluidStack.FLUID_HOLDER_STREAM_CODEC, SpiritData::fluidType,
            EffectData.STREAM_CODEC, SpiritData::effectData,
            SpiritData::new
    );

    public static SpiritData get(Level level, Holder<Fluid> fluidType) {
        return level.registryAccess()
                .lookup(DatapackRegistries.SPIRIT_REGISTRY_KEY)
                .flatMap(registry ->
                        registry.stream()
                                .filter(spiritData -> spiritData.fluidType().equals(fluidType))
                                .findFirst()
                ).orElse(Spirits.FALLBACK.get());
    }
}
