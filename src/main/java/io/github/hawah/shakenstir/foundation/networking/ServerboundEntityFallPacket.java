package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public record ServerboundEntityFallPacket(UUID entityUUID) implements ClientToServerPacket {

    public static final StreamCodec<ByteBuf, ServerboundEntityFallPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ServerboundEntityFallPacket::entityUUID,
            ServerboundEntityFallPacket::new
    );
    @Override
    public void handle(ServerPlayer player) {
        Entity entity = player.level().getEntity(entityUUID());
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(new MobEffectInstance(MobEffectRegistries.FALL_DOWN, 60));
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.ENTITY_FALL;
    }
}
