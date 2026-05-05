package io.github.hawah.shakenstir.networking;

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

    //S2C
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
