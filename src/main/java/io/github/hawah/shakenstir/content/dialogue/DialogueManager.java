package io.github.hawah.shakenstir.content.dialogue;

import com.mojang.logging.LogUtils;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 对话管理器 (Dialogue Manager)，在服务端根据当前游戏状态评估对话条件并选择要显示的对话文本。
 *
 * <p>核心逻辑：
 * <ul>
 *     <li>根据 BartenderEntity 的对话数据 (DialogueData) 中所有条目的条件逐条评估</li>
 *     <li>从满足全部条件的条目中随机选择一个</li>
 *     <li>在该条目内从尚未播放过的文本中随机选取一条（无重复播放机制）</li>
 *     <li>若该条目所有文本均已播放过，则重置已播放记录并重新选取</li>
 * </ul>
 */
public class DialogueManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 已播放索引记录 (Played Index Tracker)，按对话条目 UUID 记录哪些文本索引已被播放。
     * 存储为 NBT 映射：UUID String → BitSet 字节数组。
     */
    public static class PlayedTracker {

        private final Map<UUID, BitSet> playedIndices = new HashMap<>();

        /**
         * 检查指定对话条目中指定文本索引是否已播放。
         */
        public boolean isPlayed(UUID entryId, int textIndex) {
            BitSet bs = playedIndices.get(entryId);
            return bs != null && bs.get(textIndex);
        }

        /**
         * 标记指定对话条目中指定文本索引为已播放。
         */
        public void markPlayed(UUID entryId, int textIndex) {
            playedIndices.computeIfAbsent(entryId, k -> new BitSet()).set(textIndex);
        }

        /**
         * 获取指定条目的已播放索引集。
         */
        public BitSet getPlayed(UUID entryId) {
            return playedIndices.getOrDefault(entryId, new BitSet());
        }

        /**
         * 获取指定条目的未播放索引列表。
         */
        public List<Integer> getUnplayedIndices(UUID entryId, int totalTexts) {
            BitSet played = getPlayed(entryId);
            List<Integer> unplayed = new ArrayList<>();
            for (int i = 0; i < totalTexts; i++) {
                if (!played.get(i)) {
                    unplayed.add(i);
                }
            }
            return unplayed;
        }

        /**
         * 重置指定条目的已播放记录（所有文本重新进入可选池）。
         */
        public void resetPlayed(UUID entryId) {
            playedIndices.remove(entryId);
        }

        /**
         * 清空所有播放记录。
         */
        public void clear() {
            playedIndices.clear();
        }

        /**
         * 将此追踪器的数据序列化到 ValueOutput（用于 NBT 持久化）。
         * 格式: 每个 entry 为一个子项，key 为 UUID 字符串，value 为 BitSet 的字节数组。
         */
        public Map<UUID, BitSet> getRawData() {
            return Collections.unmodifiableMap(playedIndices);
        }

        /**
         * 从原始数据批量恢复已播放索引。
         */
        public void loadFromRaw(Map<UUID, BitSet> rawData) {
            playedIndices.clear();
            playedIndices.putAll(rawData);
        }
    }

    /**
     * 评估所有条件，选择一条合适的对话。
     *
     * @param data    对话数据
     * @param level   服务端世界
     * @param bartender 酒保实体
     * @param player  触发对话的玩家（可能为 null）
     * @param tracker 已播放索引追踪器
     * @return 选中的对话文本，如果没有符合条件的则返回 null
     */
    @Nullable
    public static Component selectDialogue(
            DialogueData data,
            ServerLevel level,
            BartenderEntity bartender,
            @Nullable Player player,
            PlayedTracker tracker
    ) {
        if (data.isEmpty()) {
            return null;
        }

        net.minecraft.util.RandomSource random = level.getRandom();

        // 第一步：筛选所有满足条件的条目
        List<DialogueEntry> matchedEntries = new ArrayList<>();
        for (DialogueEntry entry : data.entries()) {
            if (evaluateAllConditions(entry.conditions(), level, bartender, player)) {
                matchedEntries.add(entry);
            }
        }

        if (matchedEntries.isEmpty()) {
            return null;
        }

        // 第二步：随机选择一个条目
        DialogueEntry selected = matchedEntries.get(random.nextInt(matchedEntries.size()));

        // 第三步：在该条目中从未播放的文本里随机选择
        List<Integer> unplayed = tracker.getUnplayedIndices(selected.id(), selected.texts().size());

        // 如果全部已播放，则重置后重新选择
        if (unplayed.isEmpty()) {
            tracker.resetPlayed(selected.id());
            unplayed = tracker.getUnplayedIndices(selected.id(), selected.texts().size());
        }

        if (unplayed.isEmpty()) {
            // 安全后备：直接使用第一条
            return selected.texts().get(0);
        }

        int chosenIndex = unplayed.get(random.nextInt(unplayed.size()));
        tracker.markPlayed(selected.id(), chosenIndex);

        return selected.texts().get(chosenIndex);
    }

    /**
     * 评估单一条件的匹配结果。
     */
    private static boolean evaluateCondition(Condition condition, ServerLevel level, BartenderEntity bartender, @Nullable Player player) {
        return switch (condition.type()) {
            case WEATHER -> evaluateWeather(condition, level);
            case NEARBY_PLAYERS -> evaluateNearbyPlayers(condition, level, bartender);
            case INTERACTION_HISTORY -> evaluateInteractionHistory(condition, bartender, player);
            case AI_BRAIN_STATE -> evaluateAiBrainState(condition, bartender);
        };
    }

    /**
     * 评估一条 DialogueEntry 的全部条件（AND 逻辑：所有条件都必须满足）。
     */
    private static boolean evaluateAllConditions(List<Condition> conditions, ServerLevel level, BartenderEntity bartender, @Nullable Player player) {
        if (conditions.isEmpty()) {
            return true; // 无条件，总是匹配
        }
        for (Condition condition : conditions) {
            if (!evaluateCondition(condition, level, bartender, player)) {
                return false;
            }
        }
        return true;
    }

    // ========================
    //  各条件类型的评估逻辑
    // ========================

    /**
     * 评估天气条件 (Weather Condition)。
     * - operator: "is" / "is_not"
     * - value: "clear" / "rain" / "thunder"
     */
    private static boolean evaluateWeather(Condition condition, ServerLevel level) {
        String currentWeather;
        if (level.isThundering()) {
            currentWeather = "thunder";
        } else if (level.isRaining()) {
            currentWeather = "rain";
        } else {
            currentWeather = "clear";
        }

        return evaluateStringCondition(condition, currentWeather);
    }

    /**
     * 评估周围玩家条件 (Nearby Players Condition)。
     * - operator: ">=" / "<=" / "==" / ">" / "<"
     * - value: 数字
     */
    private static boolean evaluateNearbyPlayers(Condition condition, ServerLevel level, BartenderEntity bartender) {
        int nearbyCount = (int) level.players().stream()
                .filter(p -> p.distanceToSqr(bartender) <= 64.0) // 8 block radius squared
                .count();

        try {
            int threshold = Integer.parseInt(condition.value());
            return evaluateNumericCondition(condition.operator(), nearbyCount, threshold);
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid numeric value for NEARBY_PLAYERS condition: {}", condition.value());
            return false;
        }
    }

    /**
     * 评估交互历史条件 (Interaction History Condition)。
     * - operator: "is"
     * - value: "first_time" / "returning"
     */
    private static boolean evaluateInteractionHistory(Condition condition, BartenderEntity bartender, @Nullable Player player) {
        if (player == null) {
            return "first_time".equals(condition.value());
        }

        boolean hasInteracted = bartender.hasInteractedWith(player.getUUID());
        String currentState = hasInteracted ? "returning" : "first_time";

        return evaluateStringCondition(condition, currentState);
    }

    /**
     * 评估 AI 大脑状态条件 (AI Brain State Condition)。
     * - operator: "is" / "is_not"
     * - value: Activity 名称（如 "idle"、"work"、"product"、"work_idle"、"idle_front"）
     */
    private static boolean evaluateAiBrainState(Condition condition, BartenderEntity bartender) {
        String currentState = bartender.getBrain().getActiveNonCoreActivity()
                .map(Activity::getName)
                .orElse("idle");

        return evaluateStringCondition(condition, currentState);
    }

    /**
     * 字符串比较条件评估。
     */
    private static boolean evaluateStringCondition(Condition condition, String currentValue) {
        return switch (condition.operator()) {
            case "is" -> currentValue.equalsIgnoreCase(condition.value());
            case "is_not" -> !currentValue.equalsIgnoreCase(condition.value());
            default -> {
                LOGGER.warn("Unknown string operator '{}' for condition type {}", condition.operator(), condition.type());
                yield false;
            }
        };
    }

    /**
     * 数值比较条件评估。
     */
    private static boolean evaluateNumericCondition(String operator, int current, int threshold) {
        return switch (operator) {
            case ">=" -> current >= threshold;
            case "<=" -> current <= threshold;
            case "==" -> current == threshold;
            case ">" -> current > threshold;
            case "<" -> current < threshold;
            default -> {
                LOGGER.warn("Unknown numeric operator '{}'", operator);
                yield false;
            }
        };
    }
}
