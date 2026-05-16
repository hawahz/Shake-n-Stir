package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record ServerboundHandItemDataChangedPacket(UUID playerUUID, InteractionHand hand, ItemStack itemStack) implements ClientToServerPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundHandItemDataChangedPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ServerboundHandItemDataChangedPacket::playerUUID,
            InteractionHand.STREAM_CODEC, ServerboundHandItemDataChangedPacket::hand,
            ItemStack.STREAM_CODEC, ServerboundHandItemDataChangedPacket::itemStack,
            ServerboundHandItemDataChangedPacket::new
    );
    @Override
    public void handle(ServerPlayer player) {
        Player playerByUUID = player.level().getPlayerByUUID(playerUUID());
        if (playerByUUID == null) {
            return;
        }
        ItemStack item = playerByUUID.getItemInHand(hand);
        if (item.is(itemStack().getItem())) {
            item.applyComponents(itemStack.getComponents());
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.HAND_ITEM_DATA_CHANGED;
    }
}
