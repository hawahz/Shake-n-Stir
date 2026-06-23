package io.github.hawah.shakenstir.content.dialogue;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * // TODO: 人工审查 - 2026-06-23 - 新增触发模式枚举（Dialogue Trigger Mode）
 * 定义对话条目的触发模式：
 * <ul>
 *     <li>{@link #POLLING} - 周期性轮询随机触发（默认/向后兼容）</li>
 *     <li>{@link #EVENT_DRIVEN} - 事件驱动触发（特定游戏事件发生时立即触发）</li>
 * </ul>
 */
public enum DialogueTriggerMode implements StringRepresentable {
    /** 轮询随机模式：周期性评估条件后加权随机选择 */
    POLLING("polling"),
    /** 事件驱动模式：绑定特定事件，事件发生时直接匹配 */
    EVENT_DRIVEN("event_driven"),
    ;

    private final String name;

    DialogueTriggerMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static final Codec<DialogueTriggerMode> CODEC =
            StringRepresentable.fromEnum(DialogueTriggerMode::values);

    public static final StreamCodec<FriendlyByteBuf, DialogueTriggerMode> STREAM_CODEC = StreamCodec.of(
            FriendlyByteBuf::writeEnum,
            buf -> buf.readEnum(DialogueTriggerMode.class)
    );
}
