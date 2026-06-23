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
    /** 交互历史 (Interaction History) - 值: "empty" / "present"，操作符: is */
    INTERACTION_HISTORY("interaction_history"),
    // TODO: 人工审查 - 2026-06-23 - 合并 AI_BRAIN_STATE 与 CURRENT_ACTIVITY 条件类型
    //   AI_BRAIN_STATE 已删除，统一使用 CURRENT_ACTIVITY 作为唯一的活动判断条件类型。
    //   旧 JSON 配置中引用 "ai_brain_state" 的条目需手动迁移为 "current_activity"。
    /** 当前活动判断 (Current Activity) - 操作符: is / is_not, 值: 活动名称 */
    CURRENT_ACTIVITY("current_activity"),
    /** 物品寻找耗时 (Search Time) - 操作符: >= | <= | == | > | <, 值: tick 数 */
    SEARCH_TIME("search_time"),
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
