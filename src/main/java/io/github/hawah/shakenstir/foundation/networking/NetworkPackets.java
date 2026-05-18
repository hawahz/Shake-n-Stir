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
    ENTITY_FALL(ServerboundEntityFallPacket.class, ServerboundEntityFallPacket.STREAM_CODEC),
    //S2C
    SHAKE_PARAM_SYNC(ClientboundShakeParamSyncPacket.class, ClientboundShakeParamSyncPacket.STREAM_CODEC),
    UPDATE_LIVING_POSE(ClientboundUpdateLivingPosePacket.class, ClientboundUpdateLivingPosePacket.STREAM_CODEC),
    REMOVE_FORCE_PLAYER_POSE(ClientboundRemoveForcePlayerPosePacket.class, ClientboundRemoveForcePlayerPosePacket.STREAM_CODEC),
    FORCE_PLAYER_POSE(ClientboundForceSetPlayerPosePacket.class, ClientboundForceSetPlayerPosePacket.STREAM_CODEC),
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
