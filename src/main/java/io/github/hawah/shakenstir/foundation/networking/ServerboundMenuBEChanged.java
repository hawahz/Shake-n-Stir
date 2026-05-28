package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.content.blockEntity.BarMenuBlockEntity;
import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundMenuBEChanged(BarMenuBlockEntity.PriceAndCount priceAndCount, int index, BlockPos pos) implements ClientToServerPacket {

    public static final StreamCodec<ByteBuf, ServerboundMenuBEChanged> STREAM_CODEC = StreamCodec.composite(
            BarMenuBlockEntity.PriceAndCount.STREAM_CODEC, ServerboundMenuBEChanged::priceAndCount,
            ByteBufCodecs.INT, ServerboundMenuBEChanged::index,
            BlockPos.STREAM_CODEC, ServerboundMenuBEChanged::pos,
            ServerboundMenuBEChanged::new
    );
    @Override
    public void handle(ServerPlayer player) {
        if (player.level().getBlockEntity(pos) instanceof BarMenuBlockEntity blockEntity && index() >= 0 && index < blockEntity.recipes.size()) {
            blockEntity.recipes.get(index).setRight(priceAndCount);
            blockEntity.markChanged();
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.MENU_BE_CHANGED;
    }
}
