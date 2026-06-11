package io.github.hawah.shakenstir.foundation.item;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.content.block.Distiller;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.SpiritContent;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.block.DistillerPart;
import io.github.hawah.shakenstir.lib.util.Scheduler;
import io.github.hawah.shakenstir.util.ShakeClientHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SpiritBottleItem extends BlockItem implements IFluidContainer {
    public SpiritBottleItem(Block block, Properties properties) {
        super(block, properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getItemInHand().has(DataComponentTypeRegistries.UNPLACEABLE)) {
            return InteractionResult.FAIL;
        }
        BlockState blockState = context.getLevel().getBlockState(context.getClickedPos());
        if (blockState.hasProperty(Distiller.PART) && blockState.getValue(Distiller.PART).equals(DistillerPart.PIPE)) {
            return InteractionResult.FAIL;
        }
        if (blockState.getBlock() instanceof GrindstoneBlock && context.getPlayer() != null && !ItemRegistries.BOTTLE.get().equals(context.getItemInHand().getItem())) {
            SpiritContent spiritContent = context.getItemInHand().get(DataComponentTypeRegistries.SPIRIT_CONTENT);
            ItemStack bottle = ItemRegistries.BOTTLE.get().getDefaultInstance();
            bottle.set(DataComponentTypeRegistries.SPIRIT_CONTENT, spiritContent);
            context.getPlayer().setItemInHand(context.getHand(), bottle);
            context.getPlayer().playSound(
                    SoundEvents.GRINDSTONE_USE
            );
            return InteractionResult.SUCCESS;
        }
        InteractionResult result = super.useOn(context);
        if (result.equals(InteractionResult.FAIL)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return result;
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
                        itemStack.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, SpiritContent.EMPTY).isEmpty()?
                                0.01f:
                                1.0F
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
