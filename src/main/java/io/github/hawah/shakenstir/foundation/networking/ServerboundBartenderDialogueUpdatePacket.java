package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.content.dialogue.DialogueData;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

/**
 * 客户端→服务端：玩家在 DialogueEditorScreen 中编辑完成后，
 * 将修改后的对话数据写回服务端实体。
 */
public record ServerboundBartenderDialogueUpdatePacket(
        int entityId,
        DialogueData data
) implements ClientToServerPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundBartenderDialogueUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ServerboundBartenderDialogueUpdatePacket::entityId,
            DialogueData.STREAM_CODEC,
            ServerboundBartenderDialogueUpdatePacket::data,
            ServerboundBartenderDialogueUpdatePacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        if (player.level().getEntity(entityId) instanceof BartenderEntity bartender) {
            // 仅 Owner 可以修改对话数据
            if (bartender.getOwner() != null && player.is(bartender.getOwner())) {
                bartender.setDialogueData(data);
            }
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.BARTENDER_DIALOGUE_UPDATE;
    }
}
