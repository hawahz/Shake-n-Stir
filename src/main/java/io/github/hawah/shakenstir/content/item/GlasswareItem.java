package io.github.hawah.shakenstir.content.item;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.foundation.block.ITakeUpBlock;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import io.github.hawah.shakenstir.foundation.datapack.DrinkData;
import io.github.hawah.shakenstir.foundation.item.PriorityBlockItem;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GlasswareItem extends PriorityBlockItem {
    public GlasswareItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static ItemStack getMartiniGlass() {
        return SnsCreativeTab.createShortDrink("martini_glass");
    }

    public static ItemStack getMargaritaGlass() {
        return SnsCreativeTab.createShortDrink("margarita_glass");
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

    @Override
    protected boolean placeBlock(BlockPlaceContext placeContext, BlockState placementState) {
        if (!Direction.UP.equals(placeContext.getClickedFace())) {
            return false;
        }
        if (super.placeBlock(placeContext, placementState)) {
            Vec3 clickLocation = placeContext.getClickLocation().subtract(placeContext.getClickedPos().getX(), placeContext.getClickedPos().getY(), placeContext.getClickedPos().getZ());
            Vector2f localPos = new Vector2f((float) clickLocation.x(), (float) clickLocation.z());
            final float DEAD_ZONE = 0.25F;
            localPos.set(
                    Mth.clamp(localPos.x(), DEAD_ZONE, 1 - DEAD_ZONE),
                    Mth.clamp(localPos.y(), DEAD_ZONE, 1 - DEAD_ZONE)
            );
            placeContext.getItemInHand().set(DataComponentTypeRegistries.GLASSWARE_POSITION, localPos);
            assert placeContext.getPlayer() != null;
            placeContext.getItemInHand().set(DataComponentTypeRegistries.GLASSWARE_ROTATION, placeContext.getPlayer().getYRot());
            return true;
        };
        return false;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity user) {
        return itemStack.has(DataComponentTypeRegistries.DRINK_DATA)? 32: 0;
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
