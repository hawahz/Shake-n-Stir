package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.lib.networking.BasePacketPayload;

import io.github.hawah.shakenstir.lib.networking.PacketRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Locale;

public enum NetworkPackets implements BasePacketPayload.PacketTypeProvider {
    //C2S
    SHAKE_FINISH(ServerboundShakeFinishPacket.class, ServerboundShakeFinishPacket.STREAM_CODEC),
    INSERT_DECORATION(ServerboundInsertDecorationPacket.class, ServerboundInsertDecorationPacket.STREAM_CODEC),
    SHAKE_PARAM_TRANSMIT(ServerboundShakePramTransmitPacket.class, ServerboundShakePramTransmitPacket.STREAM_CODEC),
    HAND_ITEM_DATA_CHANGED(ServerboundHandItemDataChangedPacket.class, ServerboundHandItemDataChangedPacket.STREAM_CODEC),
    HAND_ITEM_AMOUNT_CHANGED(ServerboundHandItemAmountChangedPacket.class, ServerboundHandItemAmountChangedPacket.STREAM_CODEC),
    TRY_PICK_ITEM(ServerboundTryPickItemPacket.class, ServerboundTryPickItemPacket.STREAM_CODEC),
    ENTITY_FALL(ServerboundEntityFallPacket.class, ServerboundEntityFallPacket.STREAM_CODEC),
    MENU_BE_CHANGED(ServerboundMenuBEChanged.class, ServerboundMenuBEChanged.STREAM_CODEC),
    MENU_BE_RECIPE_CHANGED(ServerboundMenuBERecipeChanged.class, ServerboundMenuBERecipeChanged.STREAM_CODEC),
    MENU_BE_BKG_CHANGED(ServerboundUploadBarMenuBkgPacket.class, ServerboundUploadBarMenuBkgPacket.STREAM_CODEC),
    REQUEST_BACKGROUND(ServerboundRequestBackgroundPacket.class, ServerboundRequestBackgroundPacket.STREAM_CODEC),
    MENU_UPDATE_BKG(ServerboundMenuBlockUpdateBackgroundPacket.class, ServerboundMenuBlockUpdateBackgroundPacket.STREAM_CODEC),
    SHAKER_BUBBLED_EXPLODE(ServerboundShakerBubbledExplodePacket.class, ServerboundShakerBubbledExplodePacket.STREAM_CODEC),
    BARTENDER_SPEAK_ANNOUNCE(ServerboundBartenderSpeakAnnouncePacket.class, ServerboundBartenderSpeakAnnouncePacket.STREAM_CODEC),
    BARTENDER_DIALOGUE_UPDATE(ServerboundBartenderDialogueUpdatePacket.class, ServerboundBartenderDialogueUpdatePacket.STREAM_CODEC),
    BARTENDER_DIALOGUE_REQUEST(ServerboundBartenderDialogueRequestPacket.class, ServerboundBartenderDialogueRequestPacket.STREAM_CODEC),
    //S2C
    SHAKE_PARAM_SYNC(ClientboundShakeParamSyncPacket.class, ClientboundShakeParamSyncPacket.STREAM_CODEC),
    PLAYER_FALL_DOWN_OR_RECOVER(ClientboundPlayerFallDownOrRecoverPacket.class, ClientboundPlayerFallDownOrRecoverPacket.STREAM_CODEC),
    CLIENT_SYNC_RECIPE_DATA(ClientboundSyncRecipeData.class, ClientboundSyncRecipeData.STREAM_CODEC),
    RECEIVE_BACKGROUND(ClientboundReceiveBackgroundPacket.class, ClientboundReceiveBackgroundPacket.STREAM_CODEC),
    BARTENDER_SPEAK(ClientboundBartenderSpeakPacket.class, ClientboundBartenderSpeakPacket.STREAM_CODEC),
    BARTENDER_DIALOGUE_SYNC(ClientboundBartenderDialogueSyncPacket.class, ClientboundBartenderDialogueSyncPacket.STREAM_CODEC),
    MOB_FALL_FLY(ClientboundMobFallFlyPacket.class, ClientboundMobFallFlyPacket.STREAM_CODEC),

    // Debug Only

    // S2C
    DEBUG_BLOCK_DISPLAY(ClientboundDebugBlockDisplayPacket.class, ClientboundDebugBlockDisplayPacket.STREAM_CODEC),
    ;
    private final PacketRegistry.PacketHolder<?> type;

    <T extends BasePacketPayload> NetworkPackets(Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        String name = this.name().toLowerCase(Locale.ROOT);
        this.type = new PacketRegistry.PacketHolder<>(
                new CustomPacketPayload.Type<>(asResource(name)),
                codec, clazz
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
        return (CustomPacketPayload.Type<T>) this.type.type();
    }

    public static void register() {
        for (NetworkPackets packet : NetworkPackets.values()) {
            PacketRegistry.INSTANCE.register(packet.type);
        }
    }

    public static Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(ShakenStir.MODID, path);
    }
}
