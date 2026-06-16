package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.recipe.shake.ShakeRecipe;
import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public record ServerboundShakeFinishPacket(UUID playerUUID, ItemStack shakeItem, int shakeSuccessTimes, float pastProcess, int iceCount) implements ClientToServerPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundShakeFinishPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,  ServerboundShakeFinishPacket::playerUUID,
            ItemStack.STREAM_CODEC, ServerboundShakeFinishPacket::shakeItem,
            ByteBufCodecs.INT,      ServerboundShakeFinishPacket::shakeSuccessTimes,
            ByteBufCodecs.FLOAT,   ServerboundShakeFinishPacket::pastProcess,
            ByteBufCodecs.INT,      ServerboundShakeFinishPacket::iceCount,
            ServerboundShakeFinishPacket::new
    );

    @Override
    public void handle(ServerPlayer serverPlayer) {
        ServerLevel level = serverPlayer.level();
        Player player;
        if (!serverPlayer.getUUID().equals(playerUUID)) {
            player = level.getPlayerByUUID(playerUUID);
        } else {
            player = serverPlayer;
        }
        if (player == null) {
            return;
        }
        int shakeSuccessTimes = this.shakeSuccessTimes();
        ItemStack mainHandItem = player.getMainHandItem();
        int iceCount = iceCount();
        if (!mainHandItem.is(ItemRegistries.SHAKER)) {
            return;
        }

        ShakeRecipe.cook(shakeItem, shakeSuccessTimes, level, mainHandItem, pastProcess, iceCount, player);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.SHAKE_FINISH;
    }
}
