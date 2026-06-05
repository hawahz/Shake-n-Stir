package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.lib.networking.ServerToClientPacket;
import io.github.hawah.shakenstir.util.MenuBackgroundUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.NonNull;

public record ClientboundReceiveBackgroundPacket(String name, int[] data, int length) implements ServerToClientPacket {
    public static final StreamCodec<ByteBuf, ClientboundReceiveBackgroundPacket> STREAM_CODEC = new StreamCodec<ByteBuf, ClientboundReceiveBackgroundPacket>() {
        @Override
        public @NonNull ClientboundReceiveBackgroundPacket decode(ByteBuf input) {
            String name = ByteBufCodecs.STRING_UTF8.decode(input);
            int length = input.readInt();
            int[] data = new int[length];
            for (int i = 0; i < length; i++) {
                data[i] = input.readInt();
            }
            return new ClientboundReceiveBackgroundPacket(name, data, length);
        }

        @Override
        public void encode(ByteBuf output, ClientboundReceiveBackgroundPacket value) {

            ByteBufCodecs.STRING_UTF8.encode(output, value.name);
            output.writeInt(value.length);
            for (int i = 0; i < value.data.length; i++) {
                output.writeInt(value.data[i]);
            }
        }
    };

    @Override
    public void handle(LocalPlayer player) {
        MenuBackgroundUtils.update(name, data);
        MenuBackgroundUtils.save(name, data, false);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.RECEIVE_BACKGROUND;
    }
}
