package io.github.hawah.shakenstir.content.effect;

import com.mojang.logging.LogUtils;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import org.slf4j.Logger;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.dataAttachment.DataAttachmentTypeRegistries;
import io.github.hawah.shakenstir.foundation.networking.ClientboundMobFallFlyPacket;
import io.github.hawah.shakenstir.foundation.networking.ClientboundPlayerFallDownOrRecoverPacket;
import io.github.hawah.shakenstir.lib.ServerTaskManager;
import io.github.hawah.shakenstir.lib.networking.Networking;
import io.github.hawah.shakenstir.util.AdvancementHooks;
import io.github.hawah.shakenstir.util.Cancellable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FallDownEffect extends AbstractRemoveHookedMobEffect{
    // TODO: 人工审查 - 2026-09-03 - 新增静态 Logger 字段,替换原内联 LOGGER 调用。
    //  原代码每次写日志都会重新解析调用类并创建 Logger,改为类级静态常量后仅创建一次。
    private static final Logger LOGGER = LogUtils.getLogger();
    protected FallDownEffect(MobEffectCategory category, int color) {
        super(category, color);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ShakenStir.asResource("drunk_movement_speed"),
                -0.6,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public Cancellable onEffectRemoved(LivingEntity mob, int amplifier) {
        if (mob instanceof Player player) {
            if (!player.onGround()) {
                return Cancellable.cancel();
            }
            if (player.level() instanceof ServerLevel) {
                player.removeData(DataAttachmentTypeRegistries.FALL_DOWN);
                Networking.sendToAll(new ClientboundPlayerFallDownOrRecoverPacket(false, mob.getUUID()));
            }
        }
        return Cancellable.continua();
    }

    @Override
    public void onEffectStarted(LivingEntity mob, int amplifier) {
        if (mob instanceof Player player) {
            player.setData(DataAttachmentTypeRegistries.FALL_DOWN, 0);
            Networking.sendToAll(new ClientboundPlayerFallDownOrRecoverPacket(true, mob.getUUID()));
            AdvancementHooks.onFirstFallByDrunk(player);
        }
        MobEffectInstance effect = mob.getEffect(MobEffectRegistries.MISS_STEP);
        if (effect != null) {
            int ampl = effect.getAmplifier();
            mob.addDeltaMovement(mob.getHeadLookAngle().normalize().multiply(ampl, ampl, ampl));
            Networking.sendToAll(new ClientboundMobFallFlyPacket(mob.getUUID(), ampl));
        }
        if (mob.level() instanceof ServerLevel serverLevel) {
            int currentTicks = mob.tickCount;
            ServerTaskManager.createTask(
                    () -> mob.tickCount - currentTicks > 7,
                    () -> {
                        if (!mob.isRemoved()) {
                            try {
                                mob.hurtServer(serverLevel, mob.damageSources().fall(), 1);
                            } catch (RuntimeException e) {
                                LOGGER.error("Error while applying fall damage", e);
                            }
                        }
                        }
                    , 7);
        }
    }

}
