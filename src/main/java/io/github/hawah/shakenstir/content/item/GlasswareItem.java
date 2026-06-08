package io.github.hawah.shakenstir.content.item;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.foundation.block.ITakeUpBlock;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import io.github.hawah.shakenstir.content.recipe.datapack.DrinkData;
import io.github.hawah.shakenstir.foundation.item.PriorityBlockItem;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GlasswareItem extends PriorityBlockItem {
    public GlasswareItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static ItemStack getMartiniGlass() {
        return getShortGlass("martini_glass");
    }

    public static ItemStack getShortGlass(String name) {
        return SnsCreativeTab.createShortDrink(name);
    }

    public static ItemStack getShortGlass(Identifier glass) {
        ItemStack stack = getMartiniGlass();
        stack.set(DataComponents.ITEM_MODEL, glass);
        return stack;
    }

    public static ItemStack getMargaritaGlass() {
        return SnsCreativeTab.createShortDrink("margarita_glass");
    }
    
    public static @Nullable ItemStack getDefaultDisplay(ItemStack contentHolder) {
        if (contentHolder.has(DataComponentTypeRegistries.SHAKE_PRODUCT_POURABLE)) {
            ItemStack drop = getMartiniGlass();
            drop.set(DataComponents.ITEM_NAME, contentHolder.get(DataComponents.ITEM_NAME));
            drop.set(DataComponentTypeRegistries.SHAKE_PRODUCT_QUALITY, contentHolder.get(DataComponentTypeRegistries.SHAKE_PRODUCT_QUALITY));
            drop.set(DataComponentTypeRegistries.DRINK_DATA, contentHolder.get(DataComponentTypeRegistries.DRINK_DATA));
            drop.set(DataComponents.DYED_COLOR, contentHolder.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(0)));
            drop.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.DYED_COLOR, true));
            return drop;
        }
        return null;
    }

    @Override
    public InteractionResult place(BlockPlaceContext placeContext) {
        if (!Direction.UP.equals(placeContext.getClickedFace())) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return super.place(placeContext);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult interactionResult = super.useOn(context);
        context.getItemInHand().remove(DataComponentTypeRegistries.GLASSWARE_POSITION);
        context.getItemInHand().remove(DataComponentTypeRegistries.GLASSWARE_ROTATION);
        return interactionResult;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (itemInHand.has(DataComponentTypeRegistries.DRINK_DATA)) {
            player.startUsingItem(hand);
        }
        return super.use(level, player, hand);
    }

    public static final float DEAD_ZONE = 0.25F;

    @Override
    protected boolean placeBlock(BlockPlaceContext placeContext, BlockState placementState) {
        if (!Direction.UP.equals(placeContext.getClickedFace())) {
            return false;
        }
        if (super.placeBlock(placeContext, placementState)) {
            Vec3 clickLocation = placeContext.getClickLocation().subtract(placeContext.getClickedPos().getX(), placeContext.getClickedPos().getY(), placeContext.getClickedPos().getZ());
            Vector2f localPos = new Vector2f((float) clickLocation.x(), (float) clickLocation.z());

            localPos.set(
                    Mth.clamp(localPos.x(), DEAD_ZONE, 1 - DEAD_ZONE),
                    Mth.clamp(localPos.y(), DEAD_ZONE, 1 - DEAD_ZONE)
            );
            placeContext.getItemInHand().set(DataComponentTypeRegistries.GLASSWARE_POSITION, localPos);
            assert placeContext.getPlayer() != null;
            if (!placeContext.getItemInHand().has(DataComponentTypeRegistries.GLASSWARE_ROTATION)) {
                placeContext.getItemInHand().set(DataComponentTypeRegistries.GLASSWARE_ROTATION, placeContext.getPlayer().getYRot());
            }
            return true;
        };
        return false;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity user) {
        return itemStack.has(DataComponentTypeRegistries.DRINK_DATA)? 32: 0;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int ticksRemaining) {
        RandomSource random = level.getRandom();
        if (shouldEmitParticlesAndSounds(ticksRemaining, getUseDuration(itemStack, livingEntity))) {
            float drinkVolume = 0.5F;
            float drinkPitch = Mth.randomBetween(random, 0.9F, 1.0F);
            SoundEvent consumeSound = SoundEvents.GENERIC_DRINK.value();
            livingEntity.playSound(consumeSound, drinkVolume, drinkPitch);
        }
    }

    public boolean shouldEmitParticlesAndSounds(int useItemRemainingTicks, int useDuration) {
        int itemUsedForTicks = useDuration - useItemRemainingTicks;
        int waitTicksBeforeUseEffects = (int)(useDuration * 0.21875F);
        boolean isValidTime = itemUsedForTicks > waitTicksBeforeUseEffects;
        return isValidTime && useItemRemainingTicks % 4 == 0;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        ItemStack split = itemStack.split(1);
        if (split.has(DataComponentTypeRegistries.DRINK_DATA)) {
            DrinkData drinkData = split.get(DataComponentTypeRegistries.DRINK_DATA);
            split.remove(DataComponentTypeRegistries.DRINK_DATA);
            split.remove(DataComponents.DYED_COLOR);
            split.remove(DataComponentTypeRegistries.GLASSWARE_DECORATIONS);
            split.remove(DataComponentTypeRegistries.GLASSWARE_HAS_FLOWER);
            split.remove(DataComponentTypeRegistries.GLASSWARE_HAS_LEMON);
            split.remove(DataComponentTypeRegistries.SHAKE_PRODUCT_QUALITY);
            split.set(DataComponents.ITEM_NAME, split.getOrDefault(DataComponentTypeRegistries.GLASSWARE_NAME, LangData.ERROR));
            if (drinkData != null) {
                applyDrinkToEntity(drinkData, level, entity);
            }
            if (entity instanceof Player player) {
                ITakeUpBlock.holdOrAddItem(player, split, level, player.blockPosition());
            }
            return itemStack;
        }
        return super.finishUsingItem(itemStack, level, entity);
    }

    public static void applyDrinkToEntity(DrinkData dataComponents, Level level, LivingEntity entity) {
        dataComponents.apply(entity);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.isSameItem(oldStack, newStack);
    }
}
