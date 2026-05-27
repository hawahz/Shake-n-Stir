package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.lib.client.render.outliner.Outliner;
import io.github.hawah.shakenstir.lib.networking.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;

public record ClientboundDebugBlockDisplayPacket(BlockPos from, BlockPos to) implements ServerToClientPacket {
    public static final StreamCodec<ByteBuf, ClientboundDebugBlockDisplayPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClientboundDebugBlockDisplayPacket::from,
            BlockPos.STREAM_CODEC, ClientboundDebugBlockDisplayPacket::to,
            ClientboundDebugBlockDisplayPacket::new
    );
    @Override
    public void handle(LocalPlayer player) {
        Outliner.getInstance()
                .chaseBox(new Object(), from, to)
                .setRGBA(0, 0, 1, 1)
                .lazyDiscard(20)
                .finish();
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.DEBUG_BLOCK_DISPLAY;
    }
}
