package io.github.hawah.shakenstir.content.dialogue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 对话触发条件 (Condition)，包含条件类型、比较运算符和比较值。
 *
 * @param type     条件类型 (ConditionType)
 * @param operator 比较运算符，如 "is"、"is_not"、">="、"<=" 等
 * @param value    比较目标值，由 ConditionType 决定具体含义
 */
public record Condition(ConditionType type, String operator, String value) {

    public static final Codec<Condition> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ConditionType.CODEC.fieldOf("type").forGetter(Condition::type),
            Codec.STRING.fieldOf("operator").forGetter(Condition::operator),
            Codec.STRING.fieldOf("value").forGetter(Condition::value)
    ).apply(inst, Condition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Condition> STREAM_CODEC = StreamCodec.composite(
            ConditionType.STREAM_CODEC, Condition::type,
            ByteBufCodecs.STRING_UTF8, Condition::operator,
            ByteBufCodecs.STRING_UTF8, Condition::value,
            Condition::new
    );
}
