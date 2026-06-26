package io.github.hawah.shakenstir.foundation.datapack;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.SnsConsumables;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * {@link ConsumableDesc} 条目的内置注册表，类似于 {@code Spirits}。
 * <p>
 * 当数据包注册表不可用时（例如注册表同步前的早期工具提示渲染），
 * 提供回退查找。
 * <p>
 * 添加新消耗品描述的步骤：
 * <ol>
 *   <li>添加 {@code ResourceKey} 常量并放入 {@link #ENTRIES}</li>
 *   <li>添加本地化条目：{@code shakenstir.consumable.<descriptionKey>}</li>
 * </ol>
 */
// TODO: 人工审查 - 2026-06-27 - 新增类；集中管理全部 Consumable → 描述键映射（14 个原版 + 1 个模组），替代 SnsConsumables 硬编码 Map；提供 datapack registry 优先 + 内置回退的查找链
public class ConsumableDescs {

    // ── 资源键 ────────────────────────────────────────────────────

    // 模组消耗品
    public static final ResourceKey<ConsumableDesc> MINT = consumableDescKey("mint");

    // 原版消耗品
    public static final ResourceKey<ConsumableDesc> DEFAULT_FOOD = consumableDescKey("default_food");
    public static final ResourceKey<ConsumableDesc> DEFAULT_DRINK = consumableDescKey("default_drink");
    public static final ResourceKey<ConsumableDesc> HONEY_BOTTLE = consumableDescKey("honey_bottle");
    public static final ResourceKey<ConsumableDesc> OMINOUS_BOTTLE = consumableDescKey("ominous_bottle");
    public static final ResourceKey<ConsumableDesc> DRIED_KELP = consumableDescKey("dried_kelp");
    public static final ResourceKey<ConsumableDesc> CHICKEN = consumableDescKey("chicken");
    public static final ResourceKey<ConsumableDesc> ENCHANTED_GOLDEN_APPLE = consumableDescKey("enchanted_golden_apple");
    public static final ResourceKey<ConsumableDesc> GOLDEN_APPLE = consumableDescKey("golden_apple");
    public static final ResourceKey<ConsumableDesc> POISONOUS_POTATO = consumableDescKey("poisonous_potato");
    public static final ResourceKey<ConsumableDesc> PUFFERFISH = consumableDescKey("pufferfish");
    public static final ResourceKey<ConsumableDesc> ROTTEN_FLESH = consumableDescKey("rotten_flesh");
    public static final ResourceKey<ConsumableDesc> SPIDER_EYE = consumableDescKey("spider_eye");
    public static final ResourceKey<ConsumableDesc> MILK_BUCKET = consumableDescKey("milk_bucket");
    public static final ResourceKey<ConsumableDesc> CHORUS_FRUIT = consumableDescKey("chorus_fruit");

    // ── 内置条目 ──────────────────────────────────────────────────

    private static final Map<ResourceKey<ConsumableDesc>, ConsumableDesc> ENTRIES = new LinkedHashMap<>();

    static {
        // 模组
        ENTRIES.put(MINT, new ConsumableDesc(SnsConsumables.MINT, "mint"));

        // 原版 — 标准（无特殊效果）
        ENTRIES.put(DEFAULT_FOOD, new ConsumableDesc(Consumables.DEFAULT_FOOD, "default_food"));
        ENTRIES.put(DEFAULT_DRINK, new ConsumableDesc(Consumables.DEFAULT_DRINK, "default_drink"));

        // 原版 — 带有特殊效果
        ENTRIES.put(HONEY_BOTTLE, new ConsumableDesc(Consumables.HONEY_BOTTLE, "honey_bottle"));
        ENTRIES.put(OMINOUS_BOTTLE, new ConsumableDesc(Consumables.OMINOUS_BOTTLE, "ominous_bottle"));
        ENTRIES.put(DRIED_KELP, new ConsumableDesc(Consumables.DRIED_KELP, "dried_kelp"));
        ENTRIES.put(CHICKEN, new ConsumableDesc(Consumables.CHICKEN, "chicken"));
        ENTRIES.put(ENCHANTED_GOLDEN_APPLE, new ConsumableDesc(Consumables.ENCHANTED_GOLDEN_APPLE, "enchanted_golden_apple"));
        ENTRIES.put(GOLDEN_APPLE, new ConsumableDesc(Consumables.GOLDEN_APPLE, "golden_apple"));
        ENTRIES.put(POISONOUS_POTATO, new ConsumableDesc(Consumables.POISONOUS_POTATO, "poisonous_potato"));
        ENTRIES.put(PUFFERFISH, new ConsumableDesc(Consumables.PUFFERFISH, "pufferfish"));
        ENTRIES.put(ROTTEN_FLESH, new ConsumableDesc(Consumables.ROTTEN_FLESH, "rotten_flesh"));
        ENTRIES.put(SPIDER_EYE, new ConsumableDesc(Consumables.SPIDER_EYE, "spider_eye"));
        ENTRIES.put(MILK_BUCKET, new ConsumableDesc(Consumables.MILK_BUCKET, "milk_bucket"));
        ENTRIES.put(CHORUS_FRUIT, new ConsumableDesc(Consumables.CHORUS_FRUIT, "chorus_fruit"));
    }

    // ── 工具方法 ──────────────────────────────────────────────────

    public static ResourceKey<ConsumableDesc> consumableDescKey(String name) {
        return ResourceKey.create(Registries.CONSUMABLE_DESC, ShakenStir.asResource(name));
    }

    /** 遍历所有内置条目，用于数据生成。 */
    public static void forEachEntry(BiConsumer<ResourceKey<ConsumableDesc>, ConsumableDesc> consumer) {
        ENTRIES.forEach(consumer);
    }

    // ── 查找 ─────────────────────────────────────────────────────

    /**
     * 查找 Consumable 对应的描述键，优先从数据包注册表中查找，
     * 不可用时回退到内置映射。
     *
     * @param registries 工具提示上下文的注册表访问器（可为 null）
     * @param consumable 要查找的消耗品
     * @return 描述键后缀，未找到则返回 null
     */
    @Nullable
    public static String getDescriptionKey(@Nullable HolderLookup.Provider registries, Consumable consumable) {
        // 优先查询数据包注册表
        if (registries != null) {
            Optional<ConsumableDesc> fromRegistry = registries.lookup(Registries.CONSUMABLE_DESC)
                    .stream()
                    .flatMap(HolderLookup::listElements)
                    .map(Holder.Reference::value)
                    .filter(desc -> desc.consumable().equals(consumable))
                    .findFirst();
            if (fromRegistry.isPresent()) {
                return fromRegistry.get().descriptionKey();
            }
        }
        // 回退到内置映射
        return getBuiltIn(consumable);
    }

    /**
     * 在内置映射中搜索匹配的 Consumable。
     *
     * @return 描述键后缀，未找到则返回 null
     */
    @Nullable
    private static String getBuiltIn(Consumable consumable) {
        return ENTRIES.values().stream()
                .filter(desc -> desc.consumable().equals(consumable))
                .map(ConsumableDesc::descriptionKey)
                .findFirst()
                .orElse(null);
    }
}
