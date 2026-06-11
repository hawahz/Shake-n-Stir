package io.github.hawah.shakenstir.foundation.item;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidHolderItem extends Item implements IFluidContainer {
    public FluidHolderItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return IFluidContainer.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return IFluidContainer.getBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return IFluidContainer.getBarColor(stack);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return IFluidContainer.tryStartDrinking(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        ItemStack modifiedStack = IFluidContainer.finishDrinking(itemStack, level, entity);
        return super.finishUsingItem(modifiedStack, level, entity);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int ticksRemaining) {
        IFluidContainer.onDrinkTick(level, livingEntity, itemStack, ticksRemaining);
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity user) {
        return IFluidContainer.getUseDuration();
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return IFluidContainer.getUseAnimation();
    }
}
