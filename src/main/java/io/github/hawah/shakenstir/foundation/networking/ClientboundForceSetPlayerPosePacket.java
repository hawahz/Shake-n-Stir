package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.lib.networking.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public record ClientboundForceSetPlayerPosePacket(UUID playerUUID, Pose pose) implements ServerToClientPacket {
    public static final StreamCodec<ByteBuf, ClientboundForceSetPlayerPosePacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundForceSetPlayerPosePacket::playerUUID,
            Pose.STREAM_CODEC, ClientboundForceSetPlayerPosePacket::pose,
            ClientboundForceSetPlayerPosePacket::new
    );
    @Override
    public void handle(LocalPlayer player) {
        Player playerByUUID = player.level().getPlayerByUUID(playerUUID());
        if (playerByUUID == null) {
            return;
        }
        playerByUUID.setForcedPose(pose());
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.FORCE_PLAYER_POSE;
    }
}
