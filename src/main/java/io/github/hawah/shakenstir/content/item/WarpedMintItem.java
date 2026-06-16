package io.github.hawah.shakenstir.content.item;

import io.github.hawah.shakenstir.content.WarpedMintTooltip;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.WarpedMint;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class WarpedMintItem extends Item {
    public WarpedMintItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack self, Slot slot, ClickAction clickAction, Player player) {
//        if (ClickAction.PRIMARY.equals(clickAction) && slot.getItem().getItem() instanceof MintItem) {
//            WarpedMint warpedMint = self.getOrDefault(DataComponentTypeRegistries.WARPED_MINT, new WarpedMint());
//            warpedMint.merge(slot.getItem());
//            self.set(DataComponentTypeRegistries.WARPED_MINT, warpedMint);
//            slot.set(self);
//            return true;
//        }
        return super.overrideStackedOnOther(self, slot, clickAction, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction clickAction, Player player, SlotAccess carriedItem) {
        if (ClickAction.PRIMARY.equals(clickAction)) {
            if (other.getItem() instanceof MintItem) {
                WarpedMint warpedMint = self.getOrDefault(DataComponentTypeRegistries.WARPED_MINT, new WarpedMint());
                warpedMint.merge(other);
                other.shrink(other.getCount());
                self.set(DataComponentTypeRegistries.WARPED_MINT, warpedMint);
                return true;
            } else if (other.getItem() instanceof WarpedMintItem) {
                WarpedMint warpedMint = other.getOrDefault(DataComponentTypeRegistries.WARPED_MINT, new WarpedMint());
                WarpedMint base = self.getOrDefault(DataComponentTypeRegistries.WARPED_MINT, new WarpedMint());
                base.merge(warpedMint);
                other.shrink(other.getCount());
                self.set(DataComponentTypeRegistries.WARPED_MINT, base);
                return true;
            }
        }
        return super.overrideOtherStackedOnMe(self, other, slot, clickAction, player, carriedItem);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
        return WarpedMintTooltip.of(itemStack);
    }
}
