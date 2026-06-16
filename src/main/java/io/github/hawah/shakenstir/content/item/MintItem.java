package io.github.hawah.shakenstir.content.item;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.WarpedMint;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

@EventBusSubscriber
public class MintItem extends Item {
    public MintItem(Properties properties, int idx) {
        super(properties.component(DataComponentTypeRegistries.DECORATE_MODEL, ShakenStir.asResource("mint_deco_" + idx)));
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction clickAction, Player player, SlotAccess carriedItem) {
        if (other.is(this) && other.get(DataComponentTypeRegistries.MINT_SIZE) != self.get(DataComponentTypeRegistries.MINT_SIZE)) {
            ItemStack stack = ItemRegistries.WARPED_MINT.toStack();
            WarpedMint value = new WarpedMint();
            value.merge(self);
            value.merge(other);
            stack.set(DataComponentTypeRegistries.WARPED_MINT, value);
            slot.set(stack);
            carriedItem.set(ItemStack.EMPTY);
            return true;
        }
        return super.overrideOtherStackedOnMe(self, other, slot, clickAction, player, carriedItem);
    }

    @SubscribeEvent
    public static void onPlayerPickItem(ItemEntityPickupEvent.Pre event) {
        ItemEntity itemEntity = event.getItemEntity();
        Player player = event.getPlayer();
        ItemStack itemStack = itemEntity.getItem();
        if (itemStack.getItem() instanceof MintItem && !itemEntity.hasPickUpDelay() && (itemEntity.getTarget() == null || itemEntity.getTarget().equals(player.getUUID()))) {

            int slot = -1;
            if (!player.getInventory().hasAnyMatching(item -> item.is(ItemRegistries.WARPED_MINT))) {
                slot = player.getInventory().getFreeSlot();
            } else {
                for (int i = 0; i < player.getInventory().getNonEquipmentItems().size(); i++) {
                    if (player.getInventory().getSlot(i).get().is(ItemRegistries.WARPED_MINT)) {
                        slot = i;
                        break;
                    }
                }
            }

            if (slot < 0) {
                return;
            }

            event.setCanPickup(TriState.FALSE);
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel
                        .getChunkSource()
                        .sendToTrackingPlayers(itemEntity, new ClientboundTakeItemEntityPacket(itemEntity.getId(), player.getId(), itemStack.getCount()));
            }
            itemEntity.discard();

            SlotAccess slotAccess = player.getSlot(slot);
            if (slotAccess.get().isEmpty()) {
                ItemStack warpedMintStack = ItemRegistries.WARPED_MINT.toStack();
                WarpedMint value = new WarpedMint();
                value.merge(itemStack);
                warpedMintStack.set(DataComponentTypeRegistries.WARPED_MINT, value);

                slotAccess.set(warpedMintStack);
            } else {
                WarpedMint value = slotAccess.get().getOrDefault(DataComponentTypeRegistries.WARPED_MINT, new WarpedMint()).copy();
                value.merge(itemStack);
                slotAccess.get().set(DataComponentTypeRegistries.WARPED_MINT, value);
            }

        }
    }
}
