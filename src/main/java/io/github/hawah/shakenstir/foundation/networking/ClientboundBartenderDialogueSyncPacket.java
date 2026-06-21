package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.client.gui.DialogueEditorScreen;
import io.github.hawah.shakenstir.content.dialogue.DialogueData;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.lib.networking.ServerToClientPacket;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 服务端→客户端：将酒保的完整对话数据同步到客户端，
 * 供 DialogueEditorScreen 编辑使用。
 */
public record ClientboundBartenderDialogueSyncPacket(
        int entityId,
        DialogueData data
) implements ServerToClientPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBartenderDialogueSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ClientboundBartenderDialogueSyncPacket::entityId,
            DialogueData.STREAM_CODEC,
            ClientboundBartenderDialogueSyncPacket::data,
            ClientboundBartenderDialogueSyncPacket::new
    );

    @Override
    public void handle(LocalPlayer player) {
        if (player.level().getEntity(entityId) instanceof BartenderEntity bartender) {
            bartender.setDialogueData(data);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.BARTENDER_DIALOGUE_SYNC;
    }
}
