package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import io.github.hawah.shakenstir.util.AdvancementHooks;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

public record ServerboundShakerBubbledExplodePacket(UUID uuid) implements ClientToServerPacket {

    public static final StreamCodec<ByteBuf, ServerboundShakerBubbledExplodePacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ServerboundShakerBubbledExplodePacket::uuid,
            ServerboundShakerBubbledExplodePacket::new
    );
    @Override
    public void handle(ServerPlayer player) {
        Player playerByUUID = player.level().getPlayerByUUID(uuid());
        if (playerByUUID != null) {
            player.level().explode(null, null, null, playerByUUID.position(), 1, false, Level.ExplosionInteraction.BLOCK);
            AdvancementHooks.onShakeBubbleExplode(playerByUUID);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.SHAKER_BUBBLED_EXPLODE;
    }
}
