package io.github.hawah.shakenstir.foundation.item;

import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.lib.util.Scheduler;
import io.github.hawah.shakenstir.util.ShakeClientHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import java.awt.*;

public class SpiritBottleItem extends BlockItem {
    public SpiritBottleItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return super.useOn(context);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        FluidStack stackFluid = stack.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, FluidStack.EMPTY);
        return stackFluid.getAmount() < FluidType.BUCKET_VOLUME;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        FluidStack stackFluid = stack.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, FluidStack.EMPTY);
        return Mth.clamp((int) (((float)stackFluid.getAmount())/FluidType.BUCKET_VOLUME * 13), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        FluidStack stackFluid = stack.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, FluidStack.EMPTY);
        if (stackFluid.getFluid() instanceof WaterFluid) {
            return 0x3F76E4;
        }
        if (stackFluid.getFluid() instanceof LavaFluid) {
            return 0xF5A01A;
        }
        if (stackFluid.getFluid().is(Tags.Fluids.MILK)) {
            return 0xF3F3FF;
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
                        SoundEvents.PLAYER_SPLASH_HIGH_SPEED,
                        player.getSoundSource(),
                        1.0f,
                        1.0f
                ));
            }
        }
    }
}
