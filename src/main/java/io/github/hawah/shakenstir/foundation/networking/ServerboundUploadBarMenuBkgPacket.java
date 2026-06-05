package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import io.github.hawah.shakenstir.util.MenuBackgroundUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

public record ServerboundUploadBarMenuBkgPacket(int[] bytes, int length, String loc) implements ClientToServerPacket {
    public static final StreamCodec<ByteBuf, ServerboundUploadBarMenuBkgPacket> STREAM_CODEC = new StreamCodec<ByteBuf, ServerboundUploadBarMenuBkgPacket>() {
        @Override
        public @NonNull ServerboundUploadBarMenuBkgPacket decode(ByteBuf input) {
            int length = input.readInt();
            int[] data = new int[length];
            for (int i = 0; i < length; i++) {
                data[i] = input.readInt();
            }
            return new ServerboundUploadBarMenuBkgPacket(data, length, ByteBufCodecs.STRING_UTF8.decode(input));
        }

        @Override
        public void encode(ByteBuf output, ServerboundUploadBarMenuBkgPacket value) {
            output.writeInt(value.length);
            for (int i = 0; i < value.bytes.length; i++) {
                output.writeInt(value.bytes[i]);
            }
            ByteBufCodecs.STRING_UTF8.encode(output, value.loc);
        }
    };
    @Override
    public void handle(ServerPlayer player) {
        MenuBackgroundUtils.save(loc(), bytes(), true);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.MENU_BE_BKG_CHANGED;
    }
}
