package io.github.hawah.shakenstir.content.effect;

import io.github.hawah.shakenstir.content.damageType.SnsDamageType;
import io.github.hawah.shakenstir.content.dataAttachment.DataAttachmentTypeRegistries;
import io.github.hawah.shakenstir.content.dataAttachment.DeferredDamageAttachment;
import io.github.hawah.shakenstir.foundation.mixin.LivingEntityAccessor;
import io.github.hawah.shakenstir.foundation.tags.SnsDamageTags;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;
import java.util.Map;

@EventBusSubscriber
public class ParalysisEffect extends AbstractRemoveHookedMobEffect {
    public ParalysisEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onEffectRemoved(LivingEntity mob, int amplifier) {
        if (!mob.hasData(DataAttachmentTypeRegistries.DEFERRED_DEAD)) {
            return;
        }
        DeferredDamageAttachment data = mob.getData(DataAttachmentTypeRegistries.DEFERRED_DEAD);
        mob.removeData(DataAttachmentTypeRegistries.DEFERRED_DEAD);
        if (!data.isReady()) {
            return;
        }

        if (mob.level() instanceof ServerLevel serverLevel) {
            mob.hurtServer(serverLevel, new DamageSource(
                    serverLevel.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(SnsDamageType.PARALYSIS),
                    data.getCausingEntity(serverLevel),
                    data.getDirectEntity(serverLevel),
                    data.damageSourcePosition()
            ), mob.getHealth() + 1);
        }

    }

    public static boolean injectEffectTick(LivingEntity entity, Map<Holder<MobEffect>, MobEffectInstance> activeEffects) {
        if (activeEffects.containsKey(MobEffectRegistries.PARALYSIS)) {
            MobEffectInstance effect = activeEffects.get(MobEffectRegistries.PARALYSIS);
            if (entity.level() instanceof ServerLevel serverLevel) {
                if (!effect.tickServer(serverLevel, entity, () -> ( (LivingEntityAccessor) entity).ShakenStir$onEffectUpdated(effect, true, null))) {
                    if (!net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.MobEffectEvent.Expired(entity, effect)).isCanceled()) {
                        ( (LivingEntityAccessor) entity).ShakenStir$onEffectsRemoved(List.of(effect));
                    }
                } else if (effect.getDuration() % 600 == 0) {
                    ( (LivingEntityAccessor) entity).ShakenStir$onEffectUpdated(effect, false, null);
                }
            } else {
                effect.tickClient();

                List<ParticleOptions> particles = entity.getEntityData().get(( (LivingEntityAccessor) entity).ShakenStir$DATA_EFFECT_PARTICLES());
                if (!particles.isEmpty()) {
                    boolean isAmbient = entity.getEntityData().get(( (LivingEntityAccessor) entity).ShakenStir$DATA_EFFECT_AMBIENCE_ID());
                    int bound = entity.isInvisible() ? 15 : 4;
                    int ambientFactor = isAmbient ? 5 : 1;
                    if (entity.getRandom().nextInt(bound * ambientFactor) == 0) {
                        entity.level()
                                .addParticle(Util.getRandom(particles, entity.getRandom()), entity.getRandomX(0.5), entity.getRandomY(), entity.getRandomZ(0.5), 1.0, 1.0, 1.0);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onLivingHurtPre(LivingDamageEvent.Pre event) {
        DamageSource source = event.getSource();
        if (!source.is(SnsDamageTags.PARALYSIS_DEADLY_PREVENTION) || source.is(SnsDamageType.PARALYSIS)) {
            return;
        }
        LivingEntity entity = event.getEntity();
        float originalDamage = event.getOriginalDamage();
        if (entity.getHealth() - originalDamage > 0) {
            return;
        }
        if (entity.hasEffect(MobEffectRegistries.PARALYSIS)) {
            if (entity.hasData(DataAttachmentTypeRegistries.DEFERRED_DEAD)) {
                event.setNewDamage(0);
                return;
            }
            float newDamage = entity.getHealth() - 0.5f;
            event.setNewDamage(newDamage);
            entity.setData(DataAttachmentTypeRegistries.DEFERRED_DEAD, new DeferredDamageAttachment(source));
        }
    }
}
