package io.github.hawah.shakenstir.content.effect;

import io.github.hawah.shakenstir.util.AdvancementHooks;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.phys.AABB;

public class LemonEffect extends InstantenousMobEffect {
    private static final double BASE_SEARCH_RADIUS = 16.0;

    public LemonEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        double radius = BASE_SEARCH_RADIUS + amplifier * 4.0;
        AABB searchArea = entity.getBoundingBox().inflate(radius);

        LivingEntity nearestTarget = null;
        double nearestDistSqr = Double.MAX_VALUE;

        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, searchArea,
                e -> e != entity && e.isAlive() && entity.canAttack(e))) {
            double distSqr = entity.distanceToSqr(other);
            if (distSqr < nearestDistSqr) {
                nearestDistSqr = distSqr;
                nearestTarget = other;
            }
        }

        if (nearestTarget != null) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, nearestTarget.getEyePosition());
            if (entity instanceof Mob mob) {
                mob.setTarget(nearestTarget);
                mob.doHurtTarget(level, nearestTarget);
            } else if (entity instanceof Player player) {
                ItemStack itemStack = player.getActiveItem();
                float interactionRange = (float) player.entityInteractionRange();
                AttackRange itemAttackRange = itemStack.getOrDefault(DataComponents.ATTACK_RANGE, new AttackRange(0, interactionRange, 0, interactionRange, 0.125F, 0.5F));
                if (itemAttackRange.isInRange(player, nearestTarget.getBoundingBox(), 0)) {
                    player.attack(nearestTarget);
                    AdvancementHooks.onFirstHitDueToLemon(player);
                }
                player.swing(player.getUsedItemHand(), true);
            }
        } else {
            float randomYaw = entity.getRandom().nextFloat() * 360.0f;
            entity.setYRot(randomYaw);
            entity.setYHeadRot(randomYaw);
            entity.yBodyRot = randomYaw;
        }

        return true;
    }


}
