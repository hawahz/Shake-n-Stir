package io.github.hawah.shakenstir.content;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import io.github.hawah.shakenstir.foundation.datapack.ConsumableDescs;
import net.minecraft.core.HolderLookup;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Custom {@link Consumable} definitions for the mod, plus tooltip description lookup.
 * <p>
 * Consumable-to-description mappings are driven by the {@code shakenstir:consumable_desc}
 * datapack registry (see {@link ConsumableDescs} for built-in entries).
 */
public class SnsConsumables {
    public static final Consumable MINT = Consumables.defaultFood()
            .consumeSeconds(0.8F)
            .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
                    new MobEffectInstance(MobEffectRegistries.PARALYSIS, 10)
            )))
            .build();

    /**
     * Get the tooltip description component for a consumable.
     * Returns an empty list if:
     * <ul>
     *   <li>The consumable is not registered in the datapack registry or built-in map</li>
     *   <li>No localization exists for the current language</li>
     * </ul>
     * This ensures the tooltip is silently skipped for languages that haven't
     * translated the description.
     *
     * @param consumable the consumable to describe
     * @param registries the tooltip context registries (may be null)
     */
    public static List<Component> getDescription(Consumable consumable, @Nullable HolderLookup.Provider registries) {
        String id = ConsumableDescs.getDescriptionKey(registries, consumable);
        if (id == null) return List.of();
        String key = ShakenStir.MODID + ".consumable." + id;
        if (Language.getInstance().has(key)) {
            return List.of(Component.translatable(key));
        }
        return List.of();
    }

    /**
     * Convenience overload: always uses the built-in map (no datapack registry lookup).
     */
    public static List<Component> getDescription(Consumable consumable) {
        return getDescription(consumable, null);
    }
}
