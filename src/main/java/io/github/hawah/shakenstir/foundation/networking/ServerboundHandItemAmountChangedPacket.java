package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record ServerboundHandItemAmountChangedPacket(int amount, UUID playerUUID, InteractionHand hand) implements ClientToServerPacket {
    public static final StreamCodec<ByteBuf, ServerboundHandItemAmountChangedPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundHandItemAmountChangedPacket::amount,
            UUIDUtil.STREAM_CODEC, ServerboundHandItemAmountChangedPacket::playerUUID,
            InteractionHand.STREAM_CODEC, ServerboundHandItemAmountChangedPacket::hand,
            ServerboundHandItemAmountChangedPacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        Player playerByUUID = player.level().getPlayerByUUID(playerUUID());
        if (playerByUUID == null) {
            return;
        }
        if (amount() <= 0) {
            playerByUUID.setItemInHand(hand(), ItemStack.EMPTY);
        } else {
            playerByUUID.getItemInHand(hand()).setCount(amount());
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.HAND_ITEM_AMOUNT_CHANGED;
    }
}
