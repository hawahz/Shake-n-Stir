package io.github.hawah.shakenstir.content.item;

import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.foundation.item.DataTransportableBlockItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

public class MenuItem extends DataTransportableBlockItem {
    public MenuItem(Properties properties) {
        super(BlockRegistries.BAR_MENU_BLOCK.get(), properties);
    }

    @Override
    public void onAddingData(BlockPlaceContext placeContext, BlockState placementState) {
        ItemStack itemInHand = placeContext.getItemInHand();
        Player player = placeContext.getPlayer();
        if (player != null) {
            itemInHand.set(DataComponentTypeRegistries.PLACER, player.getUUID());
        }
    }

    @Override
    public void onRemovingData(UseOnContext context) {
        context.getItemInHand().remove(DataComponentTypeRegistries.PLACER);
    }
}
