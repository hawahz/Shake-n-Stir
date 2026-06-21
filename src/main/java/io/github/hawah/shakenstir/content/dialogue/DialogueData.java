package io.github.hawah.shakenstir.content.dialogue;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

/**
 * 对话数据 (Dialogue Data)，包含酒保实体的全部对话条目配置。
 * 作为实体持久化数据存储，同时也可通过网络同步到客户端用于编辑。
 */
public record DialogueData(List<DialogueEntry> entries) {

    public static final DialogueData EMPTY = new DialogueData(List.of());

    /**
     * 创建一个可变副本，允许编辑后重新构建。
     */
    public DialogueData copyMutable() {
        return new DialogueData(new ArrayList<>(entries));
    }

    /**
     * 获取不可变视图。
     */
    public List<DialogueEntry> getEntries() {
        return List.copyOf(entries);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    public static final Codec<DialogueData> CODEC = DialogueEntry.CODEC.listOf()
            .xmap(DialogueData::new, DialogueData::entries);

    public static final StreamCodec<RegistryFriendlyByteBuf, DialogueData> STREAM_CODEC =
            DialogueEntry.STREAM_CODEC.apply(ByteBufCodecs.list())
                    .map(DialogueData::new, DialogueData::entries);
}
