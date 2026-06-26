package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.lib.networking.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public record ClientboundMobFallFlyPacket(UUID livingId, int multiplier) implements ServerToClientPacket {

    public static final StreamCodec<ByteBuf, ClientboundMobFallFlyPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundMobFallFlyPacket::livingId,
            ByteBufCodecs.INT, ClientboundMobFallFlyPacket::multiplier,
            ClientboundMobFallFlyPacket::new
    );
    @Override
    public void handle(LocalPlayer player) {
        Entity entity = player.level().getEntity(livingId);
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.addDeltaMovement(livingEntity.getHeadLookAngle().normalize().multiply(multiplier, multiplier, multiplier));
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.MOB_FALL_FLY;
    }
}
