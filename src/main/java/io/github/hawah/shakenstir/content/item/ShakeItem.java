package io.github.hawah.shakenstir.content.item;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.content.blockEntity.ShakeBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.FluidStackDataComponent;
import io.github.hawah.shakenstir.content.dataComponent.IItemDataHolder;
import io.github.hawah.shakenstir.foundation.block.ITakeUpBlock;
import io.github.hawah.shakenstir.foundation.item.IPickMarkedItem;
import io.github.hawah.shakenstir.foundation.item.PriorityBlockItem;
import io.github.hawah.shakenstir.foundation.item.SpiritBottleItem;
import io.github.hawah.shakenstir.foundation.tags.SnsItemTags;
import io.github.hawah.shakenstir.foundation.utils.ShakeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ShakeItem extends PriorityBlockItem implements IPickMarkedItem {

    public ShakeItem(Properties properties) {
        super(BlockRegistries.SHAKE_BLOCK.get(),
                properties
                        .useCooldown(1.0F)
                        .stacksTo(1)
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
        if (player.isUsingItem()) {
            return InteractionResult.PASS;
        }
        ItemStack offhandItem = player.getOffhandItem();
        if (!offhandItem.isEmpty() && (!offhandItem.is(ItemTags.FREEZE_IMMUNE_WEARABLES) || !offhandItem.is(ItemTags.FOOT_ARMOR))) {
            return super.use(level, player, hand);
        }
        player.startUsingItem(hand);

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack shake = context.getItemInHand();
        IItemDataHolder item;
        ItemStack contentHolder;
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (
                level.getBlockEntity(pos) instanceof GlasswareBlockEntity blockEntity &&
                (item = ShakeUtil.getItemData(shake)).itemCount() == 1 &&
                        (contentHolder = item.itemStacks().getFirst()).is(ItemRegistries.CONTENT_HOLDER) &&
                        contentHolder.getOrDefault(DataComponentTypeRegistries.SHAKE_PRODUCT_POURABLE, false)
        ) {
            if (blockEntity.pourProduct(contentHolder)) {
                ShakeUtil.clearItemData(shake);
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(context);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction clickAction, Player player, SlotAccess carriedItem) {
        if (self.getOrDefault(DataComponentTypeRegistries.HAS_CUP, true)) {
            if (other.isEmpty() && clickAction.equals(ClickAction.SECONDARY)) {
                self.set(DataComponentTypeRegistries.HAS_CUP, false);
                carriedItem.set(ItemRegistries.SHAKE_CUP.toStack());
                return true;
            }
            return false;
        }
        if (other.is(ItemRegistries.SHAKE_CUP)) {
            self.set(DataComponentTypeRegistries.HAS_CUP, true);
            other.shrink(1);
            return true;
        }
        if (other.is(ItemRegistries.ICE_CUBE) && self.getOrDefault(DataComponentTypeRegistries.SHAKE_ICE_CUBES, 0) < 3) {
            self.set(DataComponentTypeRegistries.SHAKE_ICE_CUBES, self.getOrDefault(DataComponentTypeRegistries.SHAKE_ICE_CUBES, 0) + 1);
            other.shrink(1);
            return true;
        }
        if (other.is(SnsItemTags.SHAKE_PLACABLE) && ShakeUtil.getItemData(self).itemCount() < ShakeBlockEntity.MAX_HOLD_ITEMS) {
            ArrayList<ItemStack> itemStacks = new ArrayList<>(ShakeUtil.getItemStacks(self));
            itemStacks.add(other.split(1));
            ShakeUtil.setItemData(self, itemStacks);
            return true;
        }
        FluidStackDataComponent fluidStackDataComponent;
        if (
                other.getItem() instanceof SpiritBottleItem &&
                        !(fluidStackDataComponent = other.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, FluidStackDataComponent.EMPTY)).isEmpty()) {
            ArrayList<FluidStack> fluidStacks = new ArrayList<>(ShakeUtil.getFluidStacks(self));
            int sum = fluidStacks.stream().mapToInt(FluidStack::getAmount).sum();
            FluidStack fluidStack = fluidStackDataComponent.fluidStack();
            int find = -1;
            for (int i = 0; i < fluidStacks.size(); i++) {
                if (fluidStacks.get(i).is(fluidStack.getFluid())) {
                    find = i;
                    break;
                }
            }

            if (sum >= ShakeBlockEntity.MAX_FLUID_CAPACITY) {
                return false;
            }

            if (find == -1) {
                fluidStacks.add(fluidStack.split(250));
            } else {
                fluidStacks.get(find).grow(250);
                fluidStack.shrink(250);
            }

            other.set(DataComponentTypeRegistries.SPIRIT_CONTENT, fluidStack.isEmpty()? FluidStackDataComponent.EMPTY: new FluidStackDataComponent(fluidStack));
            ShakeUtil.setFluidData(self, fluidStacks);
            return true;
        }
        return super.overrideOtherStackedOnMe(self, other, slot, clickAction, player, carriedItem);
    }



    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity user) {
        return 72000;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int ticksRemaining) {
        ItemStack offhandItem = livingEntity.getOffhandItem();
        if (offhandItem.is(ItemTags.FREEZE_IMMUNE_WEARABLES) && offhandItem.is(ItemTags.FOOT_ARMOR)) {
            super.onUseTick(level, livingEntity, itemStack, ticksRemaining);
            return;
        }
        livingEntity.setIsInPowderSnow(true);
        int iceCubes = itemStack.getOrDefault(DataComponentTypeRegistries.SHAKE_ICE_CUBES, 0);
        livingEntity.setTicksFrozen(Math.min(livingEntity.getTicksRequiredToFreeze(), livingEntity.getTicksFrozen() + iceCubes));
        super.onUseTick(level, livingEntity, itemStack, ticksRemaining);
    }

    @Override
    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity entity, int remainingTime) {
        entity.setIsInPowderSnow(true);

        return true;
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        stack.remove(DataComponentTypeRegistries.SHAKING);
        super.onStopUsing(stack, entity, count);
    }


}
