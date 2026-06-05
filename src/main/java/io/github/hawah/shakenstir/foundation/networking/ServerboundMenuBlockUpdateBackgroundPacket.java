package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.content.blockEntity.BarMenuBlockEntity;
import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundMenuBlockUpdateBackgroundPacket(Identifier bkg, BlockPos pos) implements ClientToServerPacket {
    public static final StreamCodec<ByteBuf, ServerboundMenuBlockUpdateBackgroundPacket> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, ServerboundMenuBlockUpdateBackgroundPacket::bkg,
            BlockPos.STREAM_CODEC, ServerboundMenuBlockUpdateBackgroundPacket::pos,
            ServerboundMenuBlockUpdateBackgroundPacket::new
    );
    @Override
    public void handle(ServerPlayer player) {
        if (player.level().getBlockEntity(pos()) instanceof BarMenuBlockEntity blockEntity) {
            blockEntity.bkg = bkg;
            blockEntity.markChanged();
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.MENU_UPDATE_BKG;
    }
}
