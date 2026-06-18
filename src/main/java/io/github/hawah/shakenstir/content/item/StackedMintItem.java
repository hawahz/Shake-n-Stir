package io.github.hawah.shakenstir.content.item;

import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.WarpedMint;
import io.github.hawah.shakenstir.content.tooltip.WarpedMintTooltip;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class StackedMintItem extends Item {
    public StackedMintItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static String parseCount(int len) {
        String ctx;
        if (len >= 1000_000_000) {
            // ctx = len/1000 + "B";
            ctx = "inf";
        } else if (len >= 1000_000) {
            ctx = len /1000_000 + "M";
        } else if (len >= 1000) {
            ctx = len /1000 + "K";
        } else {
            ctx = String.valueOf(len);
        }
        return ctx;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack self, Slot slot, ClickAction clickAction, Player player) {
        ItemStack over = slot.getItem();
        if (over.is(ItemRegistries.MINT)) {
            WarpedMint warpedMint = self.getOrDefault(DataComponentTypeRegistries.WARPED_MINT, new WarpedMint());
            warpedMint.merge(over);
            over.shrink(over.getCount());
            self.set(DataComponentTypeRegistries.WARPED_MINT, warpedMint);
            slot.set(ItemStack.EMPTY);
            return true;
        }
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
            } else if (other.getItem() instanceof StackedMintItem) {
                WarpedMint warpedMint = other.getOrDefault(DataComponentTypeRegistries.WARPED_MINT, new WarpedMint());
                WarpedMint base = self.getOrDefault(DataComponentTypeRegistries.WARPED_MINT, new WarpedMint()).copy();
                if (!base.merge(warpedMint.copy())) {
                    return false;
                }
                other.shrink(other.getCount());
                self.set(DataComponentTypeRegistries.WARPED_MINT, base);
                return true;
            }
        } else if (ClickAction.SECONDARY.equals(clickAction)) {
            if (other.isEmpty()) {
                int select = self.getOrDefault(DataComponentTypeRegistries.SELECT_MINT, 0);
                WarpedMint warpedMint = self.getOrDefault(DataComponentTypeRegistries.WARPED_MINT, new WarpedMint()).copy();
                if (select >= 0) {
                    ItemStack itemStack = warpedMint.extract(select);
                    carriedItem.set(itemStack);
                    if (warpedMint.isEmpty()) {
                        slot.set(ItemStack.EMPTY);
                    } else if (warpedMint.variety() == 1) {
                        slot.set(warpedMint.contents().getFirst());
                    } else {
                        self.set(DataComponentTypeRegistries.WARPED_MINT, warpedMint);
                        select = Mth.clamp(select, 0, warpedMint.variety() - 1);
                        self.set(DataComponentTypeRegistries.SELECT_MINT, select);
                    }

                    return true;
                }
            }
        }
        return super.overrideOtherStackedOnMe(self, other, slot, clickAction, player, carriedItem);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
        return WarpedMintTooltip.of(itemStack);
    }
}
