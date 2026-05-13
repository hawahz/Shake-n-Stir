package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundInsertDecorationPacket(GlasswareBlockEntity.Decoration decoration, BlockPos pos) implements ClientToServerPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundInsertDecorationPacket> STREAM_CODEC = StreamCodec.composite(
            GlasswareBlockEntity.Decoration.STREAM_CODEC, ServerboundInsertDecorationPacket::decoration,
            BlockPos.STREAM_CODEC, ServerboundInsertDecorationPacket::pos,
            ServerboundInsertDecorationPacket::new
    );


    @Override
    public void handle(ServerPlayer player) {
        if (player.level().getBlockEntity(pos()) instanceof GlasswareBlockEntity blockEntity) {
            blockEntity.insertDecoration(decoration());
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.INSERT_DECORATION;
    }
}
