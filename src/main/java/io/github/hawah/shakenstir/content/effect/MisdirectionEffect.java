package io.github.hawah.shakenstir.content.effect;

// TODO: 人工审查 - 2026-06-22

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.content.effect.event.MisdirectionEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Misdirection（误导）药水效果。
 * <p>
 * 当玩家拥有此效果时，会自动将主手物品替换为最适合当前挖掘或攻击目标的物品。
 * 具体行为由 {@link MisdirectionEvents} 事件处理器驱动。
 * </p>
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MisdirectionEffect extends MobEffect {

    public MisdirectionEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        // 不需要逐tick触发，行为完全由事件驱动
        return false;
    }
}
