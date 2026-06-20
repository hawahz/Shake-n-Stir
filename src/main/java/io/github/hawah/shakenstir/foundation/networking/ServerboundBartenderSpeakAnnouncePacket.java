package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundBartenderSpeakAnnouncePacket(
        int entityId,
        Component message,
        int remainingTicks
) implements ClientToServerPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundBartenderSpeakAnnouncePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ServerboundBartenderSpeakAnnouncePacket::entityId,
            ComponentSerialization.STREAM_CODEC,
            ServerboundBartenderSpeakAnnouncePacket::message,
            ByteBufCodecs.INT,
            ServerboundBartenderSpeakAnnouncePacket::remainingTicks,
            ServerboundBartenderSpeakAnnouncePacket::new
    );

    @Override
    public void handle(ServerPlayer serverPlayer) {
        if (serverPlayer.level().getEntity(entityId) instanceof BartenderEntity bartender) {
            bartender.speakServer(message, remainingTicks);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.BARTENDER_SPEAK_ANNOUNCE;
    }
}
