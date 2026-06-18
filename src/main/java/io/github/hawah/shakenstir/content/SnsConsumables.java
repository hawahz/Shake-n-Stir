package io.github.hawah.shakenstir.content;

import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import java.awt.*;
import java.util.List;

public class SnsConsumables {
    public static final Consumable MINT = Consumables.defaultFood()
            .consumeSeconds(0.8F)
            .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
                    new MobEffectInstance(MobEffectRegistries.PARALYSIS, 10)
            )))
            .build();

    public static List<Component> getDescription(Consumable consumable) {
        return List.of();
    }
}
