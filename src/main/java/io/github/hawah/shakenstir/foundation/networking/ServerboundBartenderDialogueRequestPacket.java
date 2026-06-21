package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.content.dialogue.DialogueData;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import io.github.hawah.shakenstir.lib.networking.Networking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

/**
 * 客户端→服务端：请求服务端发送当前酒保的完整对话数据，
 * 用于在打开 DialogueEditorScreen 时获取最新数据。
 */
public record ServerboundBartenderDialogueRequestPacket(
        int entityId
) implements ClientToServerPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundBartenderDialogueRequestPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ServerboundBartenderDialogueRequestPacket::entityId,
            ServerboundBartenderDialogueRequestPacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        if (player.level().getEntity(entityId) instanceof BartenderEntity bartender) {
            // 仅 Owner 可以查看对话数据
            if (bartender.getOwner() != null && player.is(bartender.getOwner())) {
                Networking.sendToPlayer(
                        new ClientboundBartenderDialogueSyncPacket(entityId, bartender.getDialogueData()),
                        player
                );
            }
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.BARTENDER_DIALOGUE_REQUEST;
    }
}
