package io.github.hawah.shakenstir.content.effect;

import io.github.hawah.shakenstir.foundation.mixin.AbstractArrowAccess;
import io.github.hawah.shakenstir.foundation.networking.ClientboundDodgePacket;
import io.github.hawah.shakenstir.lib.networking.Networking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

        AABB mobBox = mob.getBoundingBox();
        List<Danger> dangers = new ArrayList<>();
        for (Projectile proj : projectiles) {
            if (proj instanceof AbstractArrow arrow && ((AbstractArrowAccess) arrow).shakeNStir$isInGround()) {
                continue;
            }
            Optional<Danger> danger = simulateProjectileNextFewTicks(proj, mobBox);
            danger.ifPresent(dangers::add);
        }
        Vec3 avgDir = dangers.stream()
                .map(Danger::dir)
                .reduce(Vec3.ZERO, Vec3::add)
                .normalize();
        Vec3 predDodgeDir;
        RandomSource random = serverLevel.getRandom();
        if (avgDir.y() >= 0.99) {
            predDodgeDir = Vec3.X_AXIS.yRot(random.nextFloat() * Mth.PI * 2).normalize();
        } else {
            predDodgeDir = avgDir.cross(Vec3.Y_AXIS).scale(random.nextBoolean()? 1: -1).normalize();
        }

        double speed = DODGE_SPEED;

        mob.addDeltaMovement(predDodgeDir.scale(speed));

        Networking.sendToAll(new ClientboundDodgePacket(mob.getUUID(), predDodgeDir.scale(speed)));

        return super.applyEffectTick(serverLevel, mob, amplification);
    }

    private static Optional<Danger> simulateProjectileNextFewTicks(Projectile projectile, AABB target) {
        Vec3 velocity = projectile.getDeltaMovement();
        Vec3 projPos = projectile.position();
        Vec3 targetPos = target.getCenter();
        if (targetPos.subtract(projPos).dot(velocity) < 0) {
            return Optional.empty();
        }
        AABB detectArea = target.inflate(projectile.getBbWidth(), projectile.getBbHeight(), projectile.getBbWidth());
        final int SIMULATE_TICKS = 5;
        Vec3 prevPos = projPos;
        for (int i = 0; i < SIMULATE_TICKS; i++) {
            Vec3 curPos = prevPos.add(velocity);
            Optional<Vec3> clip = detectArea.clip(prevPos, curPos);
            if (clip.isPresent()) {
                return Optional.of(new Danger(projectile, prevPos.subtract(curPos).normalize()));
            }
            prevPos = curPos;
            velocity = simulateVelocity(projectile, velocity);
        }
        return Optional.empty();
    }

    public static Vec3 simulateVelocity(Projectile projectile, Vec3 velocity) {
        if (projectile.isInWater()) {
            velocity = velocity.scale(0.6F);
        }
        boolean physicsEnabled = !projectile.noPhysics;

        if (!projectile.isInWater()) {
            velocity = velocity.scale(0.99F);
        }

        if (physicsEnabled && !(projectile instanceof AbstractArrow arrow? ( (AbstractArrowAccess) arrow).shakeNStir$isInGround(): projectile.onGround())) {
            double gravity = projectile.getGravity();
            if (gravity != 0.0) {
                velocity = velocity.add(0.0, -gravity, 0.0);
            }
        }
        return velocity;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return tickCount % 5 == 0;
    }

    record Danger(Projectile projectile, Vec3 dir) {
    }
}
