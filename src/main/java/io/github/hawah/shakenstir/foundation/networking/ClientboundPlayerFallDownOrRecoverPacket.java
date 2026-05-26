package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.content.dataAttachment.DataAttachmentTypeRegistries;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import io.github.hawah.shakenstir.lib.networking.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public record ClientboundPlayerFallDownOrRecoverPacket(boolean fallDown, UUID playerUUID) implements ServerToClientPacket {

    public static final StreamCodec<ByteBuf, ClientboundPlayerFallDownOrRecoverPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClientboundPlayerFallDownOrRecoverPacket::fallDown,
            UUIDUtil.STREAM_CODEC, ClientboundPlayerFallDownOrRecoverPacket::playerUUID,
            ClientboundPlayerFallDownOrRecoverPacket::new
    );
    @Override
    public void handle(LocalPlayer player) {
        Player playerByUUID = player.level().getPlayerByUUID(playerUUID);
        if (playerByUUID != null) {
            if (fallDown) {
                playerByUUID.setData(DataAttachmentTypeRegistries.FALL_DOWN, AnimationTickHolder.getTicks());
            } else {
                playerByUUID.removeData(DataAttachmentTypeRegistries.FALL_DOWN);
            }
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.PLAYER_FALL_DOWN_OR_RECOVER;
    }
}
