package io.github.hawah.shakenstir.content.effect;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.foundation.networking.ClientboundForceSetPlayerPosePacket;
import io.github.hawah.shakenstir.foundation.networking.ClientboundRemoveForcePlayerPosePacket;
import io.github.hawah.shakenstir.lib.networking.Networking;
import io.github.hawah.shakenstir.util.AdvancementHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FallDownEffect extends AbstractRemoveHookedMobEffect{
    protected FallDownEffect(MobEffectCategory category, int color) {
        super(category, color);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ShakenStir.asResource("drunk_movement_speed"),
                -0.25,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public void onEffectRemoved(LivingEntity mob, int amplifier) {
        if (mob instanceof Player player) {
            player.setForcedPose(null);
            if (player.level() instanceof ServerLevel) {
                Networking.sendToAll(new ClientboundRemoveForcePlayerPosePacket(mob.getUUID()));
            }
        }
    }

    @Override
    public void onEffectStarted(LivingEntity mob, int amplifier) {
        if (mob instanceof Player player) {
            player.setForcedPose(Pose.SWIMMING);
            Networking.sendToAll(new ClientboundForceSetPlayerPosePacket(mob.getUUID(), Pose.SWIMMING));
            AdvancementHooks.onFirstFallByDrunk(player);
        }
        if (mob.level() instanceof ServerLevel serverLevel) {
            mob.hurtServer(serverLevel, mob.damageSources().fall(), 1);
        }
    }

}
