package io.github.hawah.shakenstir.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public interface ITakeUpBlock {
    default boolean onUseWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.isShiftKeyDown() || !player.getMainHandItem().isEmpty()) {
            return false;
        }
        holdOrAddItem(player, getDrop(state, level, pos), level, pos);
        level.removeBlock(pos, false);
        return true;
    }
    default ItemStack getDrop(BlockState state, Level level, BlockPos pos) {
        return state.getBlock().getCloneItemStack(level, pos, state, true, null);
    }

    static void holdOrAddItem(Player player, ItemStack itemStack, Level level, BlockPos orSpawn) {
        holdOrAddItem(player, itemStack, level, orSpawn, InteractionHand.MAIN_HAND);
    }
    static void holdOrAddItem(Player player, ItemStack itemStack, Level level, BlockPos orSpawn, InteractionHand hand) {
        if (player.getItemInHand(hand).isEmpty()) {
            player.setItemInHand(hand, itemStack);
        } else if (!player.addItem(itemStack)) {
            ItemEntity drop = new ItemEntity(level, orSpawn.getX(), orSpawn.getY(), orSpawn.getZ(), itemStack);
            drop.setPickUpDelay(20);
            level.addFreshEntity(drop);
        }
    }
}
