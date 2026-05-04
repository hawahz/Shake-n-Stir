package io.github.hawah.shakenstir.lib.networking;

import net.minecraft.client.player.LocalPlayer;

public non-sealed interface ServerToClientPacket extends BasePacketPayload {
    void handle(LocalPlayer player);
}
