package io.github.hawah.shakenstir.lib.networking;

import net.minecraft.server.level.ServerPlayer;

public non-sealed interface ClientToServerPacket extends BasePacketPayload {
    void handle(ServerPlayer player);
}
