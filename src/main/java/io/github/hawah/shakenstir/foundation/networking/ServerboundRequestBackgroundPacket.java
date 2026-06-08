package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import io.github.hawah.shakenstir.lib.networking.Networking;
import io.github.hawah.shakenstir.util.MenuBackgroundUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.nio.file.Path;
import java.util.UUID;

public record ServerboundRequestBackgroundPacket(String name, UUID requester) implements ClientToServerPacket {
    public static final StreamCodec<ByteBuf, ServerboundRequestBackgroundPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerboundRequestBackgroundPacket::name,
            UUIDUtil.STREAM_CODEC, ServerboundRequestBackgroundPacket::requester,
            ServerboundRequestBackgroundPacket::new
    );
    @Override
    public void handle(ServerPlayer player) {

        Path path = MenuBackgroundUtils.BKG_SAVE_UPLOAD_PATH.resolve(name).toAbsolutePath();
        MenuBackgroundUtils.load(path).ifPresent(
                intStream -> {
                    Player playerByUUID = player.level().getPlayerByUUID(requester());
                    if (playerByUUID instanceof ServerPlayer serverPlayer) {
                        Networking.sendToPlayer(new ClientboundReceiveBackgroundPacket(name(), intStream, intStream.length), serverPlayer);
                    }
                }
        );
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.REQUEST_BACKGROUND;
    }
}
