package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import io.github.hawah.shakenstir.lib.networking.Networking;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundShakePramTransmitPacket(double x, double y, int id) implements ClientToServerPacket {
    public static final StreamCodec<ByteBuf, ServerboundShakePramTransmitPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, ServerboundShakePramTransmitPacket::x,
            ByteBufCodecs.DOUBLE, ServerboundShakePramTransmitPacket::y,
            ByteBufCodecs.INT, ServerboundShakePramTransmitPacket::id,
            ServerboundShakePramTransmitPacket::new
    );
    @Override
    public void handle(ServerPlayer player) {
        Networking.sendToAll(new ClientboundShakeParamSyncPacket(x, y, id));
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.SHAKE_PARAM_TRANSMIT;
    }
}
