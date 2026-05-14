package io.github.hawah.shakenstir.content.dataComponent;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IItemDataHolder {
    List<ItemStack> itemStacks();
    int itemCount();
}
