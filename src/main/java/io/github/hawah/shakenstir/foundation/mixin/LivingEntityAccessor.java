package io.github.hawah.shakenstir.foundation.mixin;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Collection;
import java.util.List;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Invoker("onEffectUpdated")
    void ShakenStir$onEffectUpdated(MobEffectInstance effect, boolean doRefreshAttributes, @Nullable Entity source);

    @Invoker("onEffectsRemoved")
    void ShakenStir$onEffectsRemoved(Collection<MobEffectInstance> effects);

    @Accessor("DATA_EFFECT_PARTICLES")
    EntityDataAccessor<List<ParticleOptions>> ShakenStir$DATA_EFFECT_PARTICLES();

    @Accessor("DATA_EFFECT_AMBIENCE_ID")
    EntityDataAccessor<Boolean> ShakenStir$DATA_EFFECT_AMBIENCE_ID();

}
