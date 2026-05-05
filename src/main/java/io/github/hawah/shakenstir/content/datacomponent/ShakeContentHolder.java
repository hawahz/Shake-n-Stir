package io.github.hawah.shakenstir.content.datacomponent;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemAccessItemHandler;

public class ShakeContentHolder extends ItemAccessItemHandler {
    public ShakeContentHolder(ItemAccess itemAccess, DataComponentType<ItemContainerContents> component, int size) {
        super(itemAccess, component, size);
    }
}
