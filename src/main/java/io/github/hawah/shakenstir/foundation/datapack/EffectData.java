package io.github.hawah.shakenstir.foundation.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;

import java.util.List;

public record EffectData(Holder<MobEffect> effect, List<Integer> amplifiers) {
    public static final Codec<EffectData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            MobEffect.CODEC.fieldOf("effect").forGetter(EffectData::effect),
            Codec.INT.listOf().fieldOf("amplifiers").forGetter(EffectData::amplifiers)
    ).apply(inst, EffectData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EffectData> STREAM_CODEC = StreamCodec.composite(
            MobEffect.STREAM_CODEC, EffectData::effect,
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()), EffectData::amplifiers,
            EffectData::new
    );

    public static EffectData of(Holder<MobEffect> effect, List<Integer> amplifier) {
        return new EffectData(effect, amplifier);
    }
}
