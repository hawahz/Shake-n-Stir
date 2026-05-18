package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.lib.networking.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public record ClientboundRemoveForcePlayerPosePacket(UUID playerUUID) implements ServerToClientPacket {
    public static final StreamCodec<ByteBuf, ClientboundRemoveForcePlayerPosePacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundRemoveForcePlayerPosePacket::playerUUID,
            ClientboundRemoveForcePlayerPosePacket::new
    );
    @Override
    public void handle(LocalPlayer player) {
        Player playerByUUID = player.level().getPlayerByUUID(playerUUID);
        if (playerByUUID != null) {

            playerByUUID.setForcedPose(null);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.REMOVE_FORCE_PLAYER_POSE;
    }
}
