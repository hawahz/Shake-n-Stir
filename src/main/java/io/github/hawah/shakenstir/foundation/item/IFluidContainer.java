package io.github.hawah.shakenstir.foundation.item;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.SpiritContent;
import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import io.github.hawah.shakenstir.foundation.BaseFluidType;
import io.github.hawah.shakenstir.foundation.tags.SnsFluidTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.level.material.WaterFluid;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

/**
 * 流体容器物品的共享行为工具类。
 * 由于 {@code SpiritBottleItem} 继承 {@code BlockItem}、{@code FluidHolderItem} 继承 {@code Item}，
 * 两者无法通过继承共享代码，因此将共同逻辑抽取为静态方法。
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@EventBusSubscriber
public interface IFluidContainer {

    static boolean isBarVisible(ItemStack stack) {
        if (!stack.has(DataComponentTypeRegistries.SPIRIT_CONTENT)) {
            return false;
        }
        FluidStack stackFluid = getFluidStack(stack);
        return stackFluid.getAmount() < FluidType.BUCKET_VOLUME;
    }

    static int getBarWidth(ItemStack stack) {
        FluidStack stackFluid = getFluidStack(stack);
        return Mth.clamp((int) (((float) stackFluid.getAmount()) / FluidType.BUCKET_VOLUME * 13), 0, 13);
    }

    static int getBarColor(ItemStack stack) {
        FluidStack stackFluid = getFluidStack(stack);
        if (stackFluid.getFluid() instanceof WaterFluid) {
            return 0x3F76E4;
        }
        if (stackFluid.getFluid() instanceof LavaFluid) {
            return 0xF5A01A;
        }
        if (stackFluid.getFluid().is(Tags.Fluids.MILK)) {
            return 0xF3F3FF;
        }
        if (stackFluid.getFluidType() instanceof BaseFluidType fluidType) {
            return fluidType.getTintColor();
        }
        return 0x3F76E4;
    }

    static InteractionResult tryStartDrinking(Level level, Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, SpiritContent.EMPTY).isEmpty()) {
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    static ItemStack finishDrinking(ItemStack itemStack, Level level, LivingEntity entity) {
        if (itemStack.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, SpiritContent.EMPTY).isEmpty()) {
            return itemStack;
        }
        FluidStack fluidStack = getFluidStack(itemStack);
        itemStack.set(DataComponentTypeRegistries.SPIRIT_CONTENT, new SpiritContent(fluidStack.copyWithAmount(fluidStack.getAmount() - 250)));
        if (fluidStack.is(SnsFluidTags.SPIRIT)) {
            entity.addEffect(new MobEffectInstance(
                    MobEffectRegistries.DRUNK,
                    600
            ));
        }
        return itemStack;
    }

    static void onDrinkTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int ticksRemaining) {
        if (shouldEmitParticlesAndSounds(livingEntity, itemStack, ticksRemaining)) {
            emitParticlesAndSounds(livingEntity.getRandom(), livingEntity, itemStack, 5);
        }
    }

    static void emitParticlesAndSounds(RandomSource random, LivingEntity user, ItemStack itemStack, int particleCount) {
        float drinkVolume = 0.5F;
        float drinkPitch = Mth.randomBetween(random, 0.9F, 1.0F);
        float consumableVolume = 0.5F;

        SoundEvent consumeSound = user instanceof Consumable.OverrideConsumeSound override
                ? override.getConsumeSound(itemStack)
                : SoundEvents.GENERIC_DRINK.value();
        user.playSound(consumeSound, consumableVolume, drinkPitch);
    }

    static boolean shouldEmitParticlesAndSounds(LivingEntity livingEntity, ItemStack itemStack, int useItemRemainingTicks) {
        int consumeTicks = itemStack.getUseDuration(livingEntity);
        int itemUsedForTicks = consumeTicks - useItemRemainingTicks;
        int waitTicksBeforeUseEffects = (int) (consumeTicks * 0.21875F);
        boolean isValidTime = itemUsedForTicks > waitTicksBeforeUseEffects;
        return isValidTime && useItemRemainingTicks % 4 == 0;
    }


    static int getUseDuration() {
        return 50;
    }

    static ItemUseAnimation getUseAnimation() {
        return ItemUseAnimation.DRINK;
    }

    @SubscribeEvent
    static void onGrindstoneUse(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().has(DataComponentTypeRegistries.SPIRIT_CONTENT)) {
            if (event.getLevel().getBlockState(event.getHitVec().getBlockPos()).getBlock() instanceof GrindstoneBlock) {
                event.setUseBlock(TriState.FALSE);
            }
        }
    }

    private static FluidStack getFluidStack(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, SpiritContent.EMPTY).fluidStack();
    }

    default int getColor(ItemStack itemStack) {
        if (!itemStack.has(DataComponentTypeRegistries.SPIRIT_CONTENT)) {
            return Optional.ofNullable(itemStack.get(DataComponents.DYED_COLOR))
                    .map(DyedItemColor::rgb)
                    .orElse(0x3F76E4);
        }
        return (getFluidStack(itemStack).getFluidType() instanceof BaseFluidType type)?
                type.getTintColor(): 0x3F76E4;
    }
}
