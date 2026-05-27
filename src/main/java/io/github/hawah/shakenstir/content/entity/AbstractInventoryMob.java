package io.github.hawah.shakenstir.content.entity;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Collections;

public abstract class AbstractInventoryMob extends PathfinderMob {
    protected AbstractInventoryMob(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public abstract NonNullList<ItemStack> getInventory();

    public boolean setInventorySlot(int slot, ItemStack stack) {
        if (slot < 0 || slot >= getInventory().size()) {
            return false;
        }
        getInventory().set(slot, stack);
        return true;
    }

    public ItemStack getInventorySlot(int slot) {
        if (slot < 0 || slot >= getInventory().size()) {
            return ItemStack.EMPTY;
        }
        return getInventory().get(slot);
    }

    public boolean hasInventorySlot(ItemStack stack) {
        for (ItemStack itemStack : getInventory()) {
            if (ItemStack.isSameItemSameComponents(itemStack, stack)) {
                return true;
            }
        }
        return false;
    }

    public boolean insertItem(ItemStack stack) {
        for (int i = 0; i < getInventory().size(); i++) {
            ItemStack itemStack = getInventorySlot(i);
            if (itemStack.isEmpty() && setInventorySlot(i, stack)) {
                return true;
            }
            if (ItemStack.isSameItemSameComponents(itemStack, stack)) {
                int j = Math.min(stack.getMaxStackSize(), getInventorySlotLimit(i));
                int k = Math.min(stack.getCount(), j - itemStack.getCount());
                if (k > 0) {
                    itemStack.grow(k);
                    stack.shrink(k);
                    if (stack.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int getInventorySlotLimit(int slot) {
        return 64;
    }

    public void clearInventory() {
        Collections.fill(getInventory(), ItemStack.EMPTY);
    }
}
