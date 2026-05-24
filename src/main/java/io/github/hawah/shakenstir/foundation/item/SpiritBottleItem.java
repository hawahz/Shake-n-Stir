package io.github.hawah.shakenstir.foundation.item;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.content.block.Distiller;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.SpiritContent;
import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import io.github.hawah.shakenstir.foundation.BaseFluidType;
import io.github.hawah.shakenstir.foundation.block.DistillerPart;
import io.github.hawah.shakenstir.lib.util.Scheduler;
import io.github.hawah.shakenstir.util.ShakeClientHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SpiritBottleItem extends BlockItem {
    public SpiritBottleItem(Block block, Properties properties) {
        super(block, properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState blockState = context.getLevel().getBlockState(context.getClickedPos());
        if (blockState.hasProperty(Distiller.PART) && blockState.getValue(Distiller.PART).equals(DistillerPart.PIPE)) {
            return InteractionResult.FAIL;
        }
        InteractionResult result = super.useOn(context);
        if (result.equals(InteractionResult.FAIL)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return result;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        FluidStack stackFluid = stack.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, SpiritContent.EMPTY).fluidStack();
        return stackFluid.getAmount() < FluidType.BUCKET_VOLUME;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        FluidStack stackFluid = stack.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, SpiritContent.EMPTY).fluidStack();
        return Mth.clamp((int) (((float)stackFluid.getAmount())/FluidType.BUCKET_VOLUME * 13), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        FluidStack stackFluid = stack.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, SpiritContent.EMPTY).fluidStack();
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

    @EventBusSubscriber(value = Dist.CLIENT)
    static class ClientEvents {
        @SubscribeEvent
        public static void shakeBottle (InputEvent.InteractionKeyMappingTriggered event) {
            LocalPlayer player = Minecraft.getInstance().player;
            ItemStack itemStack = player.getMainHandItem();
            boolean onCooldown = player.getCooldowns().isOnCooldown(itemStack);
            if (!onCooldown && event.isAttack() && itemStack.getItem() instanceof SpiritBottleItem && ClientDataHolder.Picker.type().equals(HitResult.Type.MISS)) {
                ShakeClientHooks.shake();
                player.getCooldowns().addCooldown(itemStack, 20);
                event.setCanceled(true);
                event.setSwingHand(false);
                Scheduler.schedule(6, () -> Minecraft.getInstance().level.playLocalSound(
                        player,
                        itemStack.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, SpiritContent.EMPTY).isEmpty()?
                                SoundEvents.PLAYER_ATTACK_SWEEP:
                                SoundEvents.BOTTLE_FILL,
                        player.getSoundSource(),
                        1.0f,
                        1.0f
                ));
            }
            if (onCooldown && itemStack.getItem() instanceof SpiritBottleItem) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, SpiritContent.EMPTY).isEmpty()) {
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        if (itemStack.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, SpiritContent.EMPTY).isEmpty()) {
            return super.finishUsingItem(itemStack, level, entity);
        }
        FluidStack fluidStack = itemStack.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, SpiritContent.EMPTY).fluidStack();
        itemStack.set(DataComponentTypeRegistries.SPIRIT_CONTENT, new SpiritContent(fluidStack.copyWithAmount(fluidStack.getAmount() - 250)));
        entity.addEffect(new MobEffectInstance(
                MobEffectRegistries.DRUNK,
                600
        ));
        return super.finishUsingItem(itemStack, level, entity);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int ticksRemaining) {
        if (shouldEmitParticlesAndSounds(livingEntity, itemStack, ticksRemaining)) {
            emitParticlesAndSounds(livingEntity.getRandom(), livingEntity, itemStack, 5);
        }
    }

    public void emitParticlesAndSounds(RandomSource random, LivingEntity user, ItemStack itemStack, int particleCount) {
        float eatVolume = random.nextBoolean() ? 0.5F : 1.0F;
        float eatPitch = random.triangle(1.0F, 0.2F);
        float drinkVolume = 0.5F;
        float drinkPitch = Mth.randomBetween(random, 0.9F, 1.0F);
        float consumableVolume = 0.5F;
//        if (this.hasConsumeParticles) {
//            user.spawnItemParticles(itemStack, particleCount);
//        }

        SoundEvent consumeSound = user instanceof Consumable.OverrideConsumeSound override ? override.getConsumeSound(itemStack) : SoundEvents.GENERIC_DRINK.value();
        user.playSound(consumeSound, consumableVolume, drinkPitch);
    }

    public boolean shouldEmitParticlesAndSounds(LivingEntity livingEntity, ItemStack itemStack, int useItemRemainingTicks) {
        int consumeTicks = itemStack.getUseDuration(livingEntity);
        int itemUsedForTicks = consumeTicks - useItemRemainingTicks;
        int waitTicksBeforeUseEffects = (int)(consumeTicks * 0.21875F);
        boolean isValidTime = itemUsedForTicks > waitTicksBeforeUseEffects;
        return isValidTime && useItemRemainingTicks % 4 == 0;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity user) {
        return 50;
    }



    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.DRINK;
    }
}
