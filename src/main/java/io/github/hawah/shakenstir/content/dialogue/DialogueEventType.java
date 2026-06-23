package io.github.hawah.shakenstir.content.dialogue;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * // TODO: 人工审查 - 2026-06-23 - 新增对话事件类型枚举（Dialogue Event Type）
 * 定义事件驱动型对话绑定的游戏事件类型：
 * <ul>
 *     <li>{@link #NONE} - 无事件（轮询模式默认值）</li>
 *     <li>{@link #ENTER_ACTIVITY} - 进入某个 AI 活动时触发</li>
 *     <li>{@link #LEAVE_ACTIVITY} - 离开某个 AI 活动时触发</li>
 *     <li>{@link #PLAYER_INTERACT} - 玩家与酒保交互时触发</li>
 * </ul>
 */
public enum DialogueEventType implements StringRepresentable {
    /** 无事件类型 - 用于轮询模式条目 */
    NONE("none"),
    /** 进入 Activity - 酒保开始执行某个 AI 活动 */
    ENTER_ACTIVITY("enter_activity"),
    /** 离开 Activity - 酒保结束执行某个 AI 活动 */
    LEAVE_ACTIVITY("leave_activity"),
    /** 玩家交互 - 玩家右键点击酒保时触发 */
    PLAYER_INTERACT("player_interact"),
    ;

    private final String name;

    DialogueEventType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static final Codec<DialogueEventType> CODEC =
            StringRepresentable.fromEnum(DialogueEventType::values);

    public static final StreamCodec<FriendlyByteBuf, DialogueEventType> STREAM_CODEC = StreamCodec.of(
            FriendlyByteBuf::writeEnum,
            buf -> buf.readEnum(DialogueEventType.class)
    );
}
