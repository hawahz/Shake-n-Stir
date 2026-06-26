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
 * Built-in registry of {@link ConsumableDesc} entries, similar to {@code Spirits}.
 * <p>
 * Provides fallback lookup when the datapack registry is unavailable (e.g. during
 * early tooltip rendering before registries are synced).
 * <p>
 * To add a new consumable description:
 * <ol>
 *   <li>Add a {@code ResourceKey} constant and put it in {@link #ENTRIES}</li>
 *   <li>Add a localization entry: {@code shakenstir.consumable.<descriptionKey>}</li>
 * </ol>
 */
public class ConsumableDescs {

    // ── Resource keys ──────────────────────────────────────────────

    // Mod consumables
    public static final ResourceKey<ConsumableDesc> MINT = consumableDescKey("mint");

    // Vanilla consumables
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

    // ── Built-in entries ───────────────────────────────────────────

    private static final Map<ResourceKey<ConsumableDesc>, ConsumableDesc> ENTRIES = new LinkedHashMap<>();

    static {
        // Mod
        ENTRIES.put(MINT, new ConsumableDesc(SnsConsumables.MINT, "mint"));

        // Vanilla — standard (no special effects)
        ENTRIES.put(DEFAULT_FOOD, new ConsumableDesc(Consumables.DEFAULT_FOOD, "default_food"));
        ENTRIES.put(DEFAULT_DRINK, new ConsumableDesc(Consumables.DEFAULT_DRINK, "default_drink"));

        // Vanilla — with effects
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

    // ── Helpers ────────────────────────────────────────────────────

    public static ResourceKey<ConsumableDesc> consumableDescKey(String name) {
        return ResourceKey.create(Registries.CONSUMABLE_DESC, ShakenStir.asResource(name));
    }

    /** Iterates all built-in entries for data generation. */
    public static void forEachEntry(BiConsumer<ResourceKey<ConsumableDesc>, ConsumableDesc> consumer) {
        ENTRIES.forEach(consumer);
    }

    // ── Lookup ─────────────────────────────────────────────────────

    /**
     * Find the description key for a Consumable, preferring the datapack registry
     * when available and falling back to the built-in map.
     *
     * @param registries the tooltip context registries (may be null)
     * @param consumable the consumable to look up
     * @return the description key suffix, or null if not found
     */
    @Nullable
    public static String getDescriptionKey(@Nullable HolderLookup.Provider registries, Consumable consumable) {
        // Try datapack registry first
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
        // Fall back to built-in map
        return getBuiltIn(consumable);
    }

    /**
     * Search the built-in map for a matching Consumable.
     *
     * @return the description key suffix, or null if not found
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
