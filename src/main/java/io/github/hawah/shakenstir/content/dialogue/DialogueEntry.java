package io.github.hawah.shakenstir.content.dialogue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.BitSet;
import java.util.List;
import java.util.UUID;

/**
 * 对话条目 (Dialogue Entry)，定义一组条件匹配后的多条可选对话文本。
 *
 * @param id            唯一标识符
 * @param conditions    触发条件列表（全部满足后本条生效）
 * @param texts         对话文本列表（条件满足时从中随机选取一条）
 * @param frequency     触发权重/冷却倍率（默认为 1，值越高越不易触发，冷却时间倍增）
 */
public record DialogueEntry(UUID id, List<Condition> conditions, List<Component> texts, int frequency) {

    /** 默认频率值 */
    public static final int DEFAULT_FREQUENCY = 1;

    public DialogueEntry {
        if (texts.isEmpty()) {
            throw new IllegalArgumentException("DialogueEntry texts must not be empty");
        }
        if (frequency <= 0) frequency = DEFAULT_FREQUENCY;
    }

    /** 向后兼容构造函数：使用默认 frequency=1 */
    public DialogueEntry(UUID id, List<Condition> conditions, List<Component> texts) {
        this(id, conditions, texts, DEFAULT_FREQUENCY);
    }

    /**
     * 创建一个带有随机 UUID 的新条目。
     */
    public static DialogueEntry create(List<Condition> conditions, List<Component> texts) {
        return new DialogueEntry(UUID.randomUUID(), conditions, texts, DEFAULT_FREQUENCY);
    }

    /**
     * 创建一个带有随机 UUID 和指定频率的新条目。
     */
    public static DialogueEntry create(List<Condition> conditions, List<Component> texts, int frequency) {
        return new DialogueEntry(UUID.randomUUID(), conditions, texts, frequency);
    }

    public static final Codec<DialogueEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("id").xmap(UUID::fromString, UUID::toString).forGetter(DialogueEntry::id),
            Condition.CODEC.listOf().fieldOf("conditions").forGetter(DialogueEntry::conditions),
            ComponentSerialization.CODEC.listOf().fieldOf("texts").forGetter(DialogueEntry::texts),
            Codec.INT.optionalFieldOf("frequency", DialogueEntry.DEFAULT_FREQUENCY).forGetter(DialogueEntry::frequency)
    ).apply(inst, DialogueEntry::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DialogueEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(Codec.STRING.xmap(UUID::fromString, UUID::toString)),
            DialogueEntry::id,
            Condition.STREAM_CODEC.apply(ByteBufCodecs.list()),
            DialogueEntry::conditions,
            ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()),
            DialogueEntry::texts,
            ByteBufCodecs.INT,
            DialogueEntry::frequency,
            DialogueEntry::new
    );
}
