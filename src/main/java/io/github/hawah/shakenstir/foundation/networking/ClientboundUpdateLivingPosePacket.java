package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.lib.networking.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

import java.util.UUID;

public record ClientboundUpdateLivingPosePacket(UUID livingUUID, Pose pose) implements ServerToClientPacket {
    public static final StreamCodec<ByteBuf, ClientboundUpdateLivingPosePacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundUpdateLivingPosePacket::livingUUID,
            Pose.STREAM_CODEC, ClientboundUpdateLivingPosePacket::pose,
            ClientboundUpdateLivingPosePacket::new
    );


    @Override
    public void handle(LocalPlayer player) {
        Entity entity = player.level().getEntity(livingUUID());
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }
        livingEntity.setPose(pose());
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.UPDATE_LIVING_POSE;
    }
}
