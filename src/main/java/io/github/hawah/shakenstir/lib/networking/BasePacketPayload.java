package io.github.hawah.shakenstir.lib.networking;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public sealed interface BasePacketPayload extends CustomPacketPayload permits ClientToServerPacket, ServerToClientPacket {
    PacketTypeProvider getTypeProvider();
    default void handleData() {
    }

    @Override
    @ApiStatus.NonExtendable
    default @NotNull Type<? extends CustomPacketPayload> type() {
        return this.getTypeProvider().getType();
    }

    interface PacketTypeProvider {
        <T extends CustomPacketPayload> Type<T> getType();
    }
}
