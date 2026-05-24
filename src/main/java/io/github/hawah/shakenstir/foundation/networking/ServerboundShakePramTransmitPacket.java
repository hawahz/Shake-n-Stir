package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import io.github.hawah.shakenstir.lib.networking.Networking;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public record ServerboundShakePramTransmitPacket(double x, double y, int id) implements ClientToServerPacket {
    public static final StreamCodec<ByteBuf, ServerboundShakePramTransmitPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, ServerboundShakePramTransmitPacket::x,
            ByteBufCodecs.DOUBLE, ServerboundShakePramTransmitPacket::y,
            ByteBufCodecs.INT, ServerboundShakePramTransmitPacket::id,
            ServerboundShakePramTransmitPacket::new
    );
    @Override
    public void handle(ServerPlayer player) {
        Entity entity = player.level().getEntity(id);
        if (entity == null) {
            return;
        }
        for (ServerPlayer serverPlayer : player.level().players()) {
            if (serverPlayer.distanceTo(entity) < 256){
                Networking.sendToPlayer(new ClientboundShakeParamSyncPacket(x, y, id), serverPlayer);
            }
        }
//        Networking.sendToAll(new ClientboundShakeParamSyncPacket(x, y, id));
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.SHAKE_PARAM_TRANSMIT;
    }
}
