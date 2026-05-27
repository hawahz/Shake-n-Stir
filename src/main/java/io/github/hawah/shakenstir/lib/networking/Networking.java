package io.github.hawah.shakenstir.lib.networking;

import com.mojang.logging.LogUtils;
import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class Networking {


    public static void sendToServer(ClientToServerPacket packet) {
        if (ShakenStir.FORCE_CHECK_SIDE) {
            if (FMLEnvironment.getDist().isDedicatedServer()) {
                LogUtils.getLogger().error("Attempted to send packet to client from server");
                return;
            }
        }
        ClientPacketDistributor.sendToServer(packet);
    }

    public static void sendToPlayer(ServerToClientPacket packet, ServerPlayer player) {
        if (ShakenStir.FORCE_CHECK_SIDE) {
            if (FMLEnvironment.getDist().isClient()) {
                LogUtils.getLogger().error("Attempted to send packet to client from client");
                return;
            }
        }
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendToAll(ServerToClientPacket packet) {
        if (ShakenStir.FORCE_CHECK_SIDE) {
            if (FMLEnvironment.getDist().isClient()) {
                LogUtils.getLogger().error("Attempted to send packet to all client from client");
                return;
            }
        }
        PacketDistributor.sendToAllPlayers(packet);
    }


    @EventBusSubscriber
    public static class Registry {

        @SubscribeEvent // on the mod event bus
        public static void register(final RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");
            for (final PacketRegistry.PacketHolder<?> packetHolder : PacketRegistry.INSTANCE.packetView) {
                Class<?> clazz = packetHolder.clazz();
                if (ClientToServerPacket.class.isAssignableFrom(clazz)) {
                    PacketRegistry.PacketHolder<ClientToServerPacket> clientToServerHolder = (PacketRegistry.PacketHolder<ClientToServerPacket>) packetHolder;
                    registrar.playToServer(
                            clientToServerHolder.type(),
                            clientToServerHolder.codec(),
                            (packet, context) -> {
                                packet.handleData();
                                context.enqueueWork(()-> packet.handle((ServerPlayer) context.player()));
                            }
                    );
                } else if (ServerToClientPacket.class.isAssignableFrom(clazz)) {
                    PacketRegistry.PacketHolder<ServerToClientPacket> serverToClientHolder = (PacketRegistry.PacketHolder<ServerToClientPacket>) packetHolder;
                    registrar.playToClient(
                            serverToClientHolder.type(),
                            serverToClientHolder.codec(),
                            (packet, context) -> {
                                packet.handleData();
                                context.enqueueWork(()-> packet.handle( (LocalPlayer) context.player()));
                            }
                    );
                }
            }

            registrar.executesOn(HandlerThread.NETWORK);
        }
    }
}
