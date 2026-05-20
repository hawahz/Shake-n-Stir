package io.github.hawah.shakenstir.content.effect;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.util.AdvancementHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DrunkEffect extends MobEffect {
    public DrunkEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity mob, int amplification) {
        return super.applyEffectTick(serverLevel, mob, amplification);
    }

    @Override
    public void onEffectAdded(LivingEntity mob, int amplifier) {
        super.onEffectAdded(mob, amplifier);
        if (mob instanceof Player player && amplifier > 5) {
            AdvancementHooks.onDrunkHeavy(player);
        }
    }



    @Override
    public void onEffectStarted(LivingEntity mob, int amplifier) {
        super.onEffectStarted(mob, amplifier);
        if (mob instanceof Player player) {
            AdvancementHooks.onFirstDrunk(player);
        }

        if (mob instanceof Player player && amplifier > 5) {
            AdvancementHooks.onDrunkHeavy(player);
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return super.shouldApplyEffectTickThisTick(tickCount, amplification);
    }
}
