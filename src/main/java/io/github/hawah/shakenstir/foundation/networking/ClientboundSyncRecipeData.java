package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.client.RecipeSyncData;
import io.github.hawah.shakenstir.foundation.data.SnsRecipeHolder;
import io.github.hawah.shakenstir.lib.networking.ServerToClientPacket;
import io.github.hawah.shakenstir.lib.signal.InstantSignal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record ClientboundSyncRecipeData(List<SnsRecipeHolder> recipes) implements ServerToClientPacket {

    public static InstantSignal SIGNAL = new InstantSignal(0);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncRecipeData> STREAM_CODEC = StreamCodec.composite(
            SnsRecipeHolder.STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundSyncRecipeData::recipes,
            ClientboundSyncRecipeData::new
    );
    @Override
    public void handle(LocalPlayer player) {
        RecipeSyncData.recipes.clear();
        RecipeSyncData.recipes.addAll(recipes);
        Minecraft.getInstance().schedule(SIGNAL::emit);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.CLIENT_SYNC_RECIPE_DATA;
    }
}
