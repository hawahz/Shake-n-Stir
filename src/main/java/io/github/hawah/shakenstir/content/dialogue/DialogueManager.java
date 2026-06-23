package io.github.hawah.shakenstir.content.dialogue;

import com.mojang.logging.LogUtils;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 对话管理器 (Dialogue Manager)，在服务端根据当前游戏状态评估对话条件并选择要显示的对话文本。
 *
 * <p>核心逻辑：
 * <ul>
 *     <li>根据 BartenderEntity 的对话数据 (DialogueData) 中所有条目的条件逐条评估</li>
 *     <li>使用频率 (frequency) 进行加权随机选择</li>
 *     <li>在该条目内从尚未播放过的文本中随机选取一条（无重复播放机制）</li>
 *     <li>支持动态变量替换 ({player_name}, {recipe_name} 等)</li>
 * </ul>
 */
public class DialogueManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    // ========================
    //  已播放索引追踪器
    // ========================

    /**
     * 已播放索引记录 (Played Index Tracker)，按对话条目 UUID 记录哪些文本索引已被播放。
     */
    public static class PlayedTracker {

        private final Map<UUID, BitSet> playedIndices = new HashMap<>();

        public boolean isPlayed(UUID entryId, int textIndex) {
            BitSet bs = playedIndices.get(entryId);
            return bs != null && bs.get(textIndex);
        }

        public void markPlayed(UUID entryId, int textIndex) {
            playedIndices.computeIfAbsent(entryId, k -> new BitSet()).set(textIndex);
        }

        public BitSet getPlayed(UUID entryId) {
            return playedIndices.getOrDefault(entryId, new BitSet());
        }

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

        public void resetPlayed(UUID entryId) {
            playedIndices.remove(entryId);
        }

        public void clear() {
            playedIndices.clear();
        }

        public Map<UUID, BitSet> getRawData() {
            return Collections.unmodifiableMap(playedIndices);
        }

        public void loadFromRaw(Map<UUID, BitSet> rawData) {
            playedIndices.clear();
            playedIndices.putAll(rawData);
        }
    }

    // ========================
    //  选择结果
    // ========================

    /**
     * 对话选择结果 (Dialogue Selection Result)，包含选中的条目和要显示的文本。
     */
    public record SelectionResult(@Nullable DialogueEntry entry, @Nullable Component text, int cooldownMultiplier) {
        public static final SelectionResult EMPTY = new SelectionResult(null, null, 1);
        public boolean hasResult() { return entry != null && text != null; }
    }

    // ========================
    //  主要选择入口
    // ========================

    /**
     * 评估所有条件，选择一条合适的对话（返回完整结果，含条目信息用于呈现模式判断）。
     * 仅考虑 {@link DialogueTriggerMode#POLLING} 模式的条目。
     */
    // TODO: 人工审查 - 2026-06-23 - selectDialogueResult 仅过滤 POLLING 模式条目，事件驱动型分离到 selectEventDialogueResult
    public static SelectionResult selectDialogueResult(
            DialogueData data,
            ServerLevel level,
            BartenderEntity bartender,
            @Nullable Player player,
            PlayedTracker tracker
    ) {
        if (data.isEmpty()) {
            return SelectionResult.EMPTY;
        }

        RandomSource random = level.getRandom();

        // 第一步：筛选所有满足条件的 POLLING 模式条目
        List<DialogueEntry> matchedEntries = new ArrayList<>();
        for (DialogueEntry entry : data.entries()) {
            if (entry.triggerMode() != DialogueTriggerMode.POLLING) continue;
            if (evaluateAllConditions(entry.conditions(), level, bartender, player)) {
                matchedEntries.add(entry);
            }
        }

        if (matchedEntries.isEmpty()) {
            return SelectionResult.EMPTY;
        }

        // 第二步：加权随机选择一个条目（权重 = 1 / frequency）
        DialogueEntry selected = weightedRandomSelect(matchedEntries, random);

        // 第三步：在该条目中从未播放的文本里随机选择
        List<Integer> unplayed = tracker.getUnplayedIndices(selected.id(), selected.texts().size());
        if (unplayed.isEmpty()) {
            tracker.resetPlayed(selected.id());
            unplayed = tracker.getUnplayedIndices(selected.id(), selected.texts().size());
        }

        if (unplayed.isEmpty()) {
            return new SelectionResult(selected, selected.texts().getFirst(), selected.frequency());
        }

        int chosenIndex = unplayed.get(random.nextInt(unplayed.size()));
        tracker.markPlayed(selected.id(), chosenIndex);

        Component rawText = selected.texts().get(chosenIndex);

        // 第四步：动态变量替换
        Component substituted = substituteVariables(rawText, level, bartender, player);

        return new SelectionResult(selected, substituted, selected.frequency());
    }

    // TODO: 人工审查 - 2026-06-23 - 新增事件驱动型对话选择方法
    //   仅考虑 EVENT_DRIVEN 模式且 eventType 匹配的条目，直接选取第一个匹配项（不进行加权随机）
    // TODO: 人工审查 - 2026-06-24 - 新增 activityContext 参数，用于在活动切换事件中准确传递旧/新活动名称

    /**
     * 事件驱动型对话选择（Event-driven Dialogue Selection）。
     * 在特定游戏事件发生时调用，直接选取第一个匹配当前事件和条件的条目，不进行加权随机选择。
     *
     * @param data            对话数据
     * @param level           服务端世界
     * @param bartender       酒保实体
     * @param player          触发玩家（可为 null）
     * @param tracker         已播放索引追踪器
     * @param eventType       当前触发的事件类型
     * @param activityContext 显式活动上下文（用于 LEAVE_ACTIVITY / ENTER_ACTIVITY 事件，null 则从 Brain 实时获取）
     * @return 选择结果，无匹配时返回 EMPTY
     */
    public static SelectionResult selectEventDialogueResult(
            DialogueData data,
            ServerLevel level,
            BartenderEntity bartender,
            @Nullable Player player,
            PlayedTracker tracker,
            DialogueEventType eventType,
            @Nullable String activityContext
    ) {
        if (data.isEmpty()) {
            return SelectionResult.EMPTY;
        }

        RandomSource random = level.getRandom();

        // 第一步：筛选匹配事件类型且条件满足的 EVENT_DRIVEN 条目
        DialogueEntry matched = null;
        for (DialogueEntry entry : data.entries()) {
            if (entry.triggerMode() != DialogueTriggerMode.EVENT_DRIVEN) continue;
            if (entry.eventType() != eventType) continue;
            if (evaluateAllConditions(entry.conditions(), level, bartender, player, activityContext)) {
                matched = entry;
                break; // 直接选取第一个匹配的条目
            }
        }

        if (matched == null) {
            return SelectionResult.EMPTY;
        }

        // 第二步：在该条目中从未播放的文本里随机选择
        List<Integer> unplayed = tracker.getUnplayedIndices(matched.id(), matched.texts().size());
        if (unplayed.isEmpty()) {
            tracker.resetPlayed(matched.id());
            unplayed = tracker.getUnplayedIndices(matched.id(), matched.texts().size());
        }

        if (unplayed.isEmpty()) {
            return new SelectionResult(matched, matched.texts().getFirst(), matched.frequency());
        }

        int chosenIndex = unplayed.get(random.nextInt(unplayed.size()));
        tracker.markPlayed(matched.id(), chosenIndex);

        Component rawText = matched.texts().get(chosenIndex);

        // 第三步：动态变量替换（传入活动上下文以正确替换 {current_activity}）
        Component substituted = substituteVariables(rawText, level, bartender, player, activityContext);

        return new SelectionResult(matched, substituted, matched.frequency());
    }

    /**
     * 评估所有条件，选择一条合适的对话（仅返回 Component，用于向后兼容）。
     */
    @Nullable
    public static Component selectDialogue(
            DialogueData data,
            ServerLevel level,
            BartenderEntity bartender,
            @Nullable Player player,
            PlayedTracker tracker
    ) {
        SelectionResult result = selectDialogueResult(data, level, bartender, player, tracker);
        return result.text();
    }

    /**
     * 根据 [BR] 标记（不区分大小写）将选中的对话文本拆分为多个独立气泡。
     * 如果文本不包含 [BR]，则返回仅包含原文本的单元素列表。
     * 每个拆分段会被 trim 处理。
     */
    public static List<Component> splitByBrMarkers(Component text) {
        String raw = text.getString();
        // 大小写不敏感的 [BR] 拆分
        String[] parts = raw.split("(?i)\\[BR\\]");
        List<Component> result = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(Component.literal(trimmed));
            }
        }
        return result.isEmpty() ? List.of(text) : result;
    }

    // ========================
    //  加权随机选择
    // ========================

    /**
     * 根据 frequency 进行加权随机选择。
     * 权重 = 1 / frequency，频率越高越不易被选中。
     */
    private static DialogueEntry weightedRandomSelect(List<DialogueEntry> entries, RandomSource random) {
        if (entries.size() == 1) return entries.getFirst();

        double totalWeight = 0;
        double[] weights = new double[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            weights[i] = 1.0 / Math.max(1, entries.get(i).frequency());
            totalWeight += weights[i];
        }

        double rand = random.nextDouble() * totalWeight;
        double cumulative = 0;
        for (int i = 0; i < entries.size(); i++) {
            cumulative += weights[i];
            if (rand <= cumulative) {
                return entries.get(i);
            }
        }
        return entries.getLast();
    }

    // ========================
    //  动态变量替换
    // ========================

    /**
     * 替换对话文本中的占位符变量为实时游戏数据。
     */
    public static Component substituteVariables(Component template, ServerLevel level, BartenderEntity bartender, @Nullable Player player) {
        return substituteVariables(template, level, bartender, player, null);
    }

    // TODO: 人工审查 - 2026-06-24 - 新增接受 activityContext 的重载方法，用于事件驱动对话在活动切换时准确替换 {current_activity}
    /**
     * 替换对话文本中的占位符变量，使用显式活动上下文替代从 Brain 实时获取的活动名。
     *
     * @param template        模板文本
     * @param level           服务端世界
     * @param bartender       酒保实体
     * @param player          玩家（可为 null）
     * @param activityContext 显式活动名称（null 则从 Brain 实时获取）
     */
    public static Component substituteVariables(Component template, ServerLevel level, BartenderEntity bartender,
                                                 @Nullable Player player, @Nullable String activityContext) {
        String text = template.getString();
        if (!text.contains("{")) return template; // 快速路径

        text = text.replace("{player_name}", player != null ? player.getName().getString() : "stranger");
        text = text.replace("{current_activity}",
                activityContext != null ? activityContext
                        : bartender.getBrain().getActiveNonCoreActivity()
                                .map(Activity::getName).orElse("idle"));
        text = text.replace("{search_elapsed_ticks}",
                String.valueOf(bartender.getSearchElapsedTicks()));

        // 配方名称
        String recipeName = "unknown recipe";
        var recipeMem = bartender.getBrain().getMemory(Memories.RECIPE.get());
        if (recipeMem.isPresent()) {
            recipeName = recipeMem.get().name();
        }
        text = text.replace("{recipe_name}", recipeName);

        // 正在寻找的物品名称
        String searchItemName = "something";
        var itemToFindMem = bartender.getBrain().getMemory(Memories.ITEM_TO_FIND.get());
        if (itemToFindMem.isPresent() && !itemToFindMem.get().isEmpty()) {
            var firstIngredient = itemToFindMem.get().getFirst();
            if (!firstIngredient.isEmpty()) {
                var stacks = firstIngredient.getValues();
                if (stacks.size() > 0) {
                    searchItemName = stacks.get(0).value().getDefaultInstance().getDisplayName().getString();
                }
            }
        }
        text = text.replace("{search_item_name}", searchItemName);

        return Component.literal(text);
    }

    // ========================
    //  条件评估
    // ========================

    private static boolean evaluateCondition(Condition condition, ServerLevel level, BartenderEntity bartender, @Nullable Player player) {
        return evaluateCondition(condition, level, bartender, player, null);
    }

    // TODO: 人工审查 - 2026-06-24 - 新增接受 activityContext 的重载，用于事件驱动对话中准确评估 CURRENT_ACTIVITY 条件
    private static boolean evaluateCondition(Condition condition, ServerLevel level, BartenderEntity bartender,
                                              @Nullable Player player, @Nullable String activityContext) {
        return switch (condition.type()) {
            case WEATHER -> evaluateWeather(condition, level);
            case NEARBY_PLAYERS -> evaluateNearbyPlayers(condition, level, bartender);
            case INTERACTION_HISTORY -> evaluateInteractionHistory(condition, bartender, player);
            // TODO: 人工审查 - 2026-06-23 - 合并 AI_BRAIN_STATE 与 CURRENT_ACTIVITY 条件类型
            case CURRENT_ACTIVITY -> evaluateCurrentActivity(condition, bartender, activityContext);
            case SEARCH_TIME -> evaluateSearchTime(condition, bartender);
        };
    }

    private static boolean evaluateAllConditions(List<Condition> conditions, ServerLevel level, BartenderEntity bartender, @Nullable Player player) {
        return evaluateAllConditions(conditions, level, bartender, player, null);
    }

    // TODO: 人工审查 - 2026-06-24 - 新增接受 activityContext 的重载
    private static boolean evaluateAllConditions(List<Condition> conditions, ServerLevel level, BartenderEntity bartender,
                                                  @Nullable Player player, @Nullable String activityContext) {
        if (conditions.isEmpty()) return true;
        for (Condition condition : conditions) {
            if (!evaluateCondition(condition, level, bartender, player, activityContext)) {
                return false;
            }
        }
        return true;
    }

    // ---- Weather ----

    private static boolean evaluateWeather(Condition condition, ServerLevel level) {
        String currentWeather;
        if (level.isThundering()) currentWeather = "thunder";
        else if (level.isRaining()) currentWeather = "rain";
        else currentWeather = "clear";
        return evaluateStringCondition(condition, currentWeather);
    }

    // ---- Nearby Players ----

    // TODO: 人工审查 - 2026-06-23 - NEARBY_PLAYERS/SEARCH_TIME 改为数值不等式运算符
    //   evaluateNearbyPlayers 已使用 evaluateNumericCondition 和 Integer.parseInt，支持 >= <= == > < 运算符
    private static boolean evaluateNearbyPlayers(Condition condition, ServerLevel level, BartenderEntity bartender) {
        int nearbyCount = (int) level.players().stream()
                .filter(p -> p.distanceToSqr(bartender) <= 64.0)
                .count();
        try {
            return evaluateNumericCondition(condition.operator(), nearbyCount, Integer.parseInt(condition.value()));
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid numeric value for NEARBY_PLAYERS: {}", condition.value());
            return false;
        }
    }

    // ---- Interaction History ----

    // TODO: 人工审查 - 2026-06-23 - INTERACTION_HISTORY 值改为 empty/present，替代旧的 first_time/returning
    //   "empty" = 从未与该玩家交互过（首次互动），"present" = 曾经与该玩家交互过（回头客）
    private static boolean evaluateInteractionHistory(Condition condition, BartenderEntity bartender, @Nullable Player player) {
        if (player == null) return "empty".equals(condition.value());
        String state = bartender.hasInteractedWith(player.getUUID()) ? "present" : "empty";
        return evaluateStringCondition(condition, state);
    }

    // ---- AI Brain State / Current Activity ----

    private static boolean evaluateCurrentActivity(Condition condition, BartenderEntity bartender) {
        return evaluateCurrentActivity(condition, bartender, null);
    }

    // TODO: 人工审查 - 2026-06-24 - 新增接受 activityContext 的重载，用于事件驱动对话在活动切换时使用正确的活动上下文
    /**
     * 评估当前活动条件。如果 activityContext 非空则直接使用；否则从 Brain 实时获取。
     */
    private static boolean evaluateCurrentActivity(Condition condition, BartenderEntity bartender,
                                                    @Nullable String activityContext) {
        String current;
        if (activityContext != null) {
            current = activityContext;
        } else {
            current = bartender.getBrain().getActiveNonCoreActivity()
                    .map(Activity::getName).orElse("idle");
        }
        return evaluateStringCondition(condition, current);
    }

    // ---- Search Time ----

    // TODO: 人工审查 - 2026-06-23 - NEARBY_PLAYERS/SEARCH_TIME 改为数值不等式运算符
    //   evaluateSearchTime 已使用 evaluateNumericCondition 和 Integer.parseInt，支持 >= <= == > < 运算符
    /**
     * 评估物品寻找耗时条件，读取 BartenderEntity 中追踪的 searchElapsedTicks。
     */
    private static boolean evaluateSearchTime(Condition condition, BartenderEntity bartender) {
        int searchTicks = bartender.getSearchElapsedTicks();
        try {
            return evaluateNumericCondition(condition.operator(), searchTicks, Integer.parseInt(condition.value()));
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid numeric value for SEARCH_TIME: {}", condition.value());
            return false;
        }
    }

    // ========================
    //  通用评估辅助方法
    // ========================

    private static boolean evaluateStringCondition(Condition condition, String currentValue) {
        return switch (condition.operator()) {
            case "is" -> currentValue.equalsIgnoreCase(condition.value());
            case "is_not" -> !currentValue.equalsIgnoreCase(condition.value());
            default -> {
                LOGGER.warn("Unknown string operator '{}'", condition.operator());
                yield false;
            }
        };
    }

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
