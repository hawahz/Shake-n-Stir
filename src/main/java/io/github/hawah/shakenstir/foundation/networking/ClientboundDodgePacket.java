package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.lib.networking.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record ClientboundDodgePacket(UUID host, Vec3 delta) implements ServerToClientPacket {
    public static final StreamCodec<ByteBuf, ClientboundDodgePacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundDodgePacket::host,
            Vec3.STREAM_CODEC, ClientboundDodgePacket::delta,
            ClientboundDodgePacket::new
    );
    @Override
    public void handle(LocalPlayer player) {
        Entity entity = player.level().getEntity(host);
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.addDeltaMovement(delta);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.DODGE;
    }
}
