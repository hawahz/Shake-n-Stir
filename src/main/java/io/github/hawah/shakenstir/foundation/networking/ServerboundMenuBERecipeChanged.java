package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.content.blockEntity.BarMenuBlockEntity;
import io.github.hawah.shakenstir.content.data.SnsRecipeHolder;
import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundMenuBERecipeChanged(SnsRecipeHolder recipe, int index, BlockPos pos) implements ClientToServerPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundMenuBERecipeChanged> STREAM_CODEC = StreamCodec.composite(
            SnsRecipeHolder.STREAM_CODEC, ServerboundMenuBERecipeChanged::recipe,
            ByteBufCodecs.INT, ServerboundMenuBERecipeChanged::index,
            BlockPos.STREAM_CODEC, ServerboundMenuBERecipeChanged::pos,
            ServerboundMenuBERecipeChanged::new
    );
    @Override
    public void handle(ServerPlayer player) {
        if (player.level().getBlockEntity(pos) instanceof BarMenuBlockEntity blockEntity && index() >= 0 && index < blockEntity.recipes.size()) {
            blockEntity.setRecipe(index(), recipe());
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.MENU_BE_RECIPE_CHANGED;
    }
}
