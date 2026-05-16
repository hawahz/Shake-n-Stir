package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.client.ClientSharedShakeParams;
import io.github.hawah.shakenstir.lib.networking.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClientboundShakeParamSyncPacket(double x, double y, int id) implements ServerToClientPacket {

    public static final StreamCodec<ByteBuf, ClientboundShakeParamSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, ClientboundShakeParamSyncPacket::x,
            ByteBufCodecs.DOUBLE, ClientboundShakeParamSyncPacket::y,
            ByteBufCodecs.INT, ClientboundShakeParamSyncPacket::id,
            ClientboundShakeParamSyncPacket::new
    );
    @Override
    public void handle(LocalPlayer player) {
        ClientSharedShakeParams.updateParam(id, x, y);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.SHAKE_PARAM_SYNC;
    }
}
