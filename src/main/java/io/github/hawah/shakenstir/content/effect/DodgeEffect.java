package io.github.hawah.shakenstir.content.effect;

import io.github.hawah.shakenstir.foundation.networking.ClientboundDodgePacket;
import io.github.hawah.shakenstir.lib.networking.Networking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

public class DodgeEffect extends MobEffect {
    private static final Predicate<Entity> ALWAYS_TRUE = _ -> true;
    private static final float DODGE_SPEED = 0.8F;
    private static final double DETECT_RANGE = 10.0;
    private static final double VERTICAL_BOOST = 0.2;
    private static final double MIN_HORIZONTAL_SPEED = 0.001;

    protected DodgeEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity mob, int amplification) {
        AABB detectArea = mob.getBoundingBox().inflate(DETECT_RANGE);
        List<Projectile> projectiles = serverLevel.getEntities(
                EntityTypeTest.forClass(Projectile.class),
                detectArea,
                ALWAYS_TRUE
        );
        if (projectiles.isEmpty()) {
            return super.applyEffectTick(serverLevel, mob, amplification);
        }

        // Use swept-AABB intersection instead of thin raycast:
        // a projectile's bounding box expanded in its direction of movement
        // is its full swept volume this tick — much more accurate than a center-line ray.
        AABB mobBox = mob.getBoundingBox();
        Projectile target = null;
        for (Projectile proj : projectiles) {
            Vec3 delta = proj.getDeltaMovement();
            AABB sweptBox = proj.getBoundingBox().expandTowards(delta);
            if (sweptBox.intersects(mobBox)) {
                target = proj;
                break;
            }
        }

        if (target != null) {
            Vec3 projVel = target.getDeltaMovement();
            Vec3 dodgeDir;

            double horizSpeedSq = projVel.x * projVel.x + projVel.z * projVel.z;
            if (horizSpeedSq > MIN_HORIZONTAL_SPEED * MIN_HORIZONTAL_SPEED) {
                // Dodge perpendicular to projectile velocity in the horizontal plane.
                // Randomly pick left or right perpendicular — both are 90° to the
                // projectile's path and equally safe, but variety feels more natural.
                boolean right = serverLevel.getRandom().nextBoolean();
                dodgeDir = new Vec3(
                        right ? -projVel.z : projVel.z,
                        0,
                        right ? projVel.x : -projVel.x
                ).normalize();
            } else {
                // Projectile is moving almost purely vertically.
                // Dodge in a random horizontal direction since there's no meaningful
                // horizontal velocity to compute a perpendicular from.
                double angle = serverLevel.getRandom().nextDouble() * 2 * Math.PI;
                dodgeDir = new Vec3(Math.cos(angle), 0, Math.sin(angle));
            }

            var dodgeDelta = dodgeDir.scale(DODGE_SPEED).add(0, VERTICAL_BOOST, 0);
            mob.addDeltaMovement(dodgeDelta);
            Networking.sendToAll(new ClientboundDodgePacket(mob.getUUID(), dodgeDelta));
        }

        return super.applyEffectTick(serverLevel, mob, amplification);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return true;
    }
}
