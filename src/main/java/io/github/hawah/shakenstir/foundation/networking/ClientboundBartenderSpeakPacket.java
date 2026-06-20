package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.lib.networking.ServerToClientPacket;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClientboundBartenderSpeakPacket(
        int entityId,
        Component message,
        int remainingTicks
) implements ServerToClientPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBartenderSpeakPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ClientboundBartenderSpeakPacket::entityId,
            ComponentSerialization.STREAM_CODEC,
            ClientboundBartenderSpeakPacket::message,
            ByteBufCodecs.INT,
            ClientboundBartenderSpeakPacket::remainingTicks,
            ClientboundBartenderSpeakPacket::new
    );

    @Override
    public void handle(LocalPlayer player) {
        if (player.level().getEntity(entityId) instanceof BartenderEntity bartender) {
            bartender.speakClient(message, false, remainingTicks);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.BARTENDER_SPEAK;
    }
}
