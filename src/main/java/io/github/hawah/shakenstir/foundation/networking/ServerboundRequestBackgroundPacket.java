package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import io.github.hawah.shakenstir.lib.networking.Networking;
import io.github.hawah.shakenstir.util.MenuBackgroundUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

public record ServerboundRequestBackgroundPacket(String name, UUID requester) implements ClientToServerPacket {
    public static final StreamCodec<ByteBuf, ServerboundRequestBackgroundPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerboundRequestBackgroundPacket::name,
            UUIDUtil.STREAM_CODEC, ServerboundRequestBackgroundPacket::requester,
            ServerboundRequestBackgroundPacket::new
    );
    @Override
    public void handle(ServerPlayer player) {

        Path path = MenuBackgroundUtils.BKG_SAVE_UPLOAD_PATH.resolve(name).toAbsolutePath();
        if (Files.exists(path)) {
            try (DataInputStream stream = new DataInputStream(new BufferedInputStream(
                    new GZIPInputStream(Files.newInputStream(path, StandardOpenOption.READ))))) {
                CompoundTag nbt = NbtIo.read(stream, NbtAccounter.create(0x20000000L));
                nbt.getIntArray("data").ifPresent(
                        intStream -> {
                            Player playerByUUID = player.level().getPlayerByUUID(requester());
                            if (playerByUUID instanceof ServerPlayer serverPlayer) {
                                Networking.sendToPlayer(new ClientboundReceiveBackgroundPacket(name(), intStream, intStream.length), serverPlayer);
                            }
                        }
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.REQUEST_BACKGROUND;
    }
}
