package io.github.hawah.shakenstir.content.item;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.block.Shake;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.foundation.block.ITakeUpBlock;
import io.github.hawah.shakenstir.foundation.item.IPickMarkedItem;
import io.github.hawah.shakenstir.foundation.item.PriorityBlockItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ShakeItem extends PriorityBlockItem implements IPickMarkedItem {

    public ShakeItem(Properties properties) {
        super(BlockRegistries.SHAKE_BLOCK.get(),
                properties
                        .useCooldown(1.0F)
                        .stacksTo(1)
                        .component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                        .component(DataComponentTypeRegistries.HAS_CUP, true)
        );
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            ItemStack shakeItem = player.getItemInHand(hand);
            InteractionHand otherHand = hand.equals(InteractionHand.MAIN_HAND) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            if (shakeItem.getOrDefault(DataComponentTypeRegistries.HAS_CUP, false)){
                shakeItem.set(DataComponentTypeRegistries.HAS_CUP, false);
                ITakeUpBlock.holdOrAddItem(player, ItemRegistries.SHAKE_CUP.toStack(), level, player.blockPosition(), otherHand);
                return InteractionResult.PASS;
            } else if (player.getItemInHand(otherHand).is(ItemRegistries.SHAKE_CUP)) {
                player.getItemInHand(otherHand).shrink(1);
                shakeItem.set(DataComponentTypeRegistries.HAS_CUP, true);
                player.getCooldowns().addCooldown(shakeItem, 20);
                return InteractionResult.PASS;
            }
        }
        if (!player.getItemInHand(hand).getOrDefault(DataComponentTypeRegistries.HAS_CUP, false)) {
            return InteractionResult.PASS;
        }
        if (player.getMainHandItem().getOrDefault(DataComponentTypeRegistries.SHAKING, false) && player.isUsingItem()) {
            return InteractionResult.PASS;
        }
        player.getMainHandItem().set(DataComponentTypeRegistries.SHAKING, true);
        player.startUsingItem(hand);

        return InteractionResult.PASS;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity user) {
        return 72000;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int ticksRemaining) {
        livingEntity.setIsInPowderSnow(true);
        livingEntity.setTicksFrozen(Math.min(livingEntity.getTicksRequiredToFreeze(), livingEntity.getTicksFrozen() + 1));
        super.onUseTick(level, livingEntity, itemStack, ticksRemaining);
    }

    @Override
    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity entity, int remainingTime) {

        itemStack.remove(DataComponentTypeRegistries.SHAKING);

        return true;
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        stack.remove(DataComponentTypeRegistries.SHAKING);
        super.onStopUsing(stack, entity, count);
    }


}
