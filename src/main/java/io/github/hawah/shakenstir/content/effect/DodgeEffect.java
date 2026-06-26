package io.github.hawah.shakenstir.content.effect;

import io.github.hawah.shakenstir.foundation.networking.ClientboundDodgePacket;
import io.github.hawah.shakenstir.lib.networking.Networking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DodgeEffect extends MobEffect {
    protected DodgeEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity mob, int amplification) {
        AABB detectArea = mob.getBoundingBox().inflate(10);
        List<Projectile> projectiles = serverLevel.getEntities(
                EntityTypeTest.forClass(Projectile.class),
                detectArea,
                _ -> true
        );
        if (!projectiles.isEmpty()) {
            Projectile projec = null;
            for (Projectile proj : projectiles) {
                Vec3 delta = proj.getDeltaMovement();
                Vec3 from = proj.position();
                Vec3 to = from.add(proj.getDeltaMovement());
                AABB searchArea = proj.getBoundingBox().expandTowards(delta.multiply(2, 2, 2))
                        .inflate(2.0);

                EntityHitResult res = ProjectileUtil.getEntityHitResult(
                        serverLevel,
                        proj,
                        from,
                        to,
                        searchArea,
                        entity -> !entity.isSpectator() && entity.isPickable() && entity instanceof LivingEntity
                );
                if (res == null) {
                    continue;
                }
                Entity entity = res.getEntity();
                if (!entity.equals(mob)) {
                    continue;
                }
                projec = proj;
                break;
            }
            if (projec != null) {
                float scale = 0.8F;
                var delta = projec.position().subtract(mob.position())
                        .cross(new Vec3(0, serverLevel.getRandom().nextFloat() < 0.5? 1 : -1, 0))
                        .normalize()
                        .multiply(scale, scale, scale);
                mob.addDeltaMovement(delta);
                Networking.sendToAll(new ClientboundDodgePacket(mob.getUUID(), delta));
            }
        }
        return super.applyEffectTick(serverLevel, mob, amplification);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return true;
    }
}
