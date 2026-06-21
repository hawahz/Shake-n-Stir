package io.github.hawah.shakenstir.content.dialogue;

import com.mojang.serialization.Codec;
import io.github.hawah.shakenstir.util.SerializeHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * 对话条件类型 (Condition Type)，定义触发对话的判断维度。
 */
public enum ConditionType implements StringRepresentable {
    /** 当前天气 (Weather) - 值: "clear" / "rain" / "thunder" */
    WEATHER("weather"),
    /** 周围玩家数量 (Nearby Players) - 值: 数字字符串，配合 operator 使用 */
    NEARBY_PLAYERS("nearby_players"),
    /** 交互历史 (Interaction History) - 值: "first_time" / "returning" */
    INTERACTION_HISTORY("interaction_history"),
    /** AI 大脑当前活动 (AI Brain State/Activity) - 值: Activity 名称 */
    AI_BRAIN_STATE("ai_brain_state"),
    ;

    private final String name;

    ConditionType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static final Codec<ConditionType> CODEC = StringRepresentable.fromEnum(ConditionType::values);

    public static final StreamCodec<FriendlyByteBuf, ConditionType> STREAM_CODEC = StreamCodec.of(
            (buf, type) -> buf.writeEnum(type),
            buf -> buf.readEnum(ConditionType.class)
    );
}
