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
 */
public record DialogueEntry(UUID id, List<Condition> conditions, List<Component> texts) {

    public DialogueEntry {
        // 确保 texts 不为空
        if (texts.isEmpty()) {
            throw new IllegalArgumentException("DialogueEntry texts must not be empty");
        }
    }

    /**
     * 创建一个带有随机 UUID 的新条目。
     */
    public static DialogueEntry create(List<Condition> conditions, List<Component> texts) {
        return new DialogueEntry(UUID.randomUUID(), conditions, texts);
    }

    public static final Codec<DialogueEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("id").xmap(UUID::fromString, UUID::toString).forGetter(DialogueEntry::id),
            Condition.CODEC.listOf().fieldOf("conditions").forGetter(DialogueEntry::conditions),
            ComponentSerialization.CODEC.listOf().fieldOf("texts").forGetter(DialogueEntry::texts)
    ).apply(inst, DialogueEntry::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DialogueEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(Codec.STRING.xmap(UUID::fromString, UUID::toString)),
            DialogueEntry::id,
            Condition.STREAM_CODEC.apply(ByteBufCodecs.list()),
            DialogueEntry::conditions,
            ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()),
            DialogueEntry::texts,
            DialogueEntry::new
    );
}
