package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public record ServerboundTryPickItemPacket(ItemStack itemStack) implements ClientToServerPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundTryPickItemPacket> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, ServerboundTryPickItemPacket::itemStack,
            ServerboundTryPickItemPacket::new
    );
    @Override
    public void handle(ServerPlayer player) {
        if (itemStack.isItemEnabled(player.level().enabledFeatures())) {
            Inventory inventory = player.getInventory();
            int slotWithExistingItem = inventory.findSlotMatchingItem(itemStack);
            if (slotWithExistingItem != -1) {
                if (Inventory.isHotbarSlot(slotWithExistingItem)) {
                    inventory.setSelectedSlot(slotWithExistingItem);
                } else {
                    inventory.pickSlot(slotWithExistingItem);
                }
            } else if (player.hasInfiniteMaterials()) {
                inventory.addAndPickItem(itemStack);
            }

            player.connection.send(new ClientboundSetHeldSlotPacket(inventory.getSelectedSlot()));
            player.inventoryMenu.broadcastChanges();
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.TRY_PICK_ITEM;
    }
}
