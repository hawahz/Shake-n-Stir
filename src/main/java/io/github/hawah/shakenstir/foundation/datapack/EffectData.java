package io.github.hawah.shakenstir.foundation.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.content.recipe.Quality;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * 效果数据记录类，用于存储正面和负面效果的配置信息。
 * 包含效果类型、等级和持续时间的数据序列化支持。
 *
 * @param positive           正面效果的MobEffect持有者
 * @param negative           负面效果的MobEffect持有者
 * @param positiveLevNDur    正面效果的等级和持续时间映射列表
 * @param negativeLevNDur    负面效果的等级和持续时间映射列表
 */
public record EffectData(
        Holder<MobEffect> positive,
        Holder<MobEffect> negative,
        List<LevelMapDuration> positiveLevNDur,
        List<LevelMapDuration> negativeLevNDur
) {
    public static final Codec<EffectData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            MobEffect.CODEC.fieldOf("positive").forGetter(EffectData::positive),
            MobEffect.CODEC.fieldOf("negative").forGetter(EffectData::negative),
            LevelMapDuration.CODEC.listOf().fieldOf("positiveLevNDur").forGetter(EffectData::positiveLevNDur),
            LevelMapDuration.CODEC.listOf().fieldOf("negativeLevNDur").forGetter(EffectData::negativeLevNDur)
    ).apply(inst, EffectData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EffectData> STREAM_CODEC = StreamCodec.composite(
            MobEffect.STREAM_CODEC, EffectData::positive,
            MobEffect.STREAM_CODEC, EffectData::negative,
            LevelMapDuration.STREAM_CODEC.apply(ByteBufCodecs.list()), EffectData::positiveLevNDur,
            LevelMapDuration.STREAM_CODEC.apply(ByteBufCodecs.list()), EffectData::negativeLevNDur,
            EffectData::new
    );

    public MobEffectInstance getPositive(int level) {
        level = Math.min(level, positiveLevNDur().size() - 1);
        return new MobEffectInstance(positive(), positiveLevNDur().get(level).duration(), positiveLevNDur().get(level).level());
    }

    public MobEffectInstance getNegative(int level) {
        level = Math.min(level, negativeLevNDur().size() - 1);
        return new MobEffectInstance(negative(), negativeLevNDur().get(level).duration(), negativeLevNDur().get(level).level());
    }

    public MobEffectInstance get(int signedIndex) {
        if (signedIndex >= 0) {
            return getPositive(Mth.clamp(signedIndex, 0, positiveLevNDur().size() - 1));
        } else {
            return getNegative(Mth.clamp(-signedIndex - 1, 0, negativeLevNDur().size() - 1));
        }
    }

    public MobEffectInstance get(Quality quality) {
        return get(quality.toSignedIndex());
    }

    // TODO Remove
    public static EffectData of(Holder<MobEffect> effect, List<Integer> amplifier) {
        return new EffectData(effect, effect, LevelMapDuration.of(amplifier, 1200), List.of());
    }
    public static EffectData of(Holder<MobEffect> positive, Holder<MobEffect> negative, List<Integer> amplifier, List<Integer> duration, List<Integer> negativeAmplifier, List<Integer> negativeDuration) {
        return new EffectData(positive, negative, LevelMapDuration.of(amplifier, duration), LevelMapDuration.of(negativeAmplifier, negativeDuration));
    }
    public static EffectData spirit(Holder<MobEffect> positive, Holder<MobEffect> negative) {
        return of(
                positive, negative,
                List.of(0, 0, 1, 1, 2), List.of(90 * 20, 120 * 20, 120 * 20, 150 * 20, 180 * 20),
                List.of(0, 0, 1, 2), List.of(20 * 20, 30 * 20, 45 * 20, 60 * 20)
        );
    }

    public record LevelMapDuration(int level, int duration) {
        public static final Codec<LevelMapDuration> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.fieldOf("level").forGetter(LevelMapDuration::level),
                Codec.INT.fieldOf("duration").forGetter(LevelMapDuration::duration)
        ).apply(inst, LevelMapDuration::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, LevelMapDuration> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, LevelMapDuration::level,
                ByteBufCodecs.INT, LevelMapDuration::duration,
                LevelMapDuration::new
        );

        public static List<LevelMapDuration> of(List<Integer> amplifier, int duration) {
            return amplifier.stream().map(integer -> new LevelMapDuration(duration, integer)).toList();
        }
        public static List<LevelMapDuration> of(List<Integer> amplifier, List<Integer> duration) {
            List<LevelMapDuration> ret = new ArrayList<>();
            for (int i = 0; i < Math.max(amplifier.size(), duration.size()); i++) {
                ret.add(new LevelMapDuration(safeGet(amplifier, i), safeGet(duration, i)));
            }
            return ret;
        }

        private static <T> T safeGet(List<T> list, int index) {
            return index < list.size() ?
                    index >= 0 ?
                            list.get(index) :
                            list.getFirst():
                    list.getLast();
        }
    }
}
