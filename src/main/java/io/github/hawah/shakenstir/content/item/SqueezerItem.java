package io.github.hawah.shakenstir.content.item;

import io.github.hawah.shakenstir.content.block.SpiritBlock;
import io.github.hawah.shakenstir.content.blockEntity.ShakeBlockEntity;
import io.github.hawah.shakenstir.content.blockEntity.SpiritBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.SingleItemComponent;
import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import io.github.hawah.shakenstir.foundation.tags.SnsItemTags;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class SqueezerItem extends Item {
    public SqueezerItem(Properties properties) {
        super(
                properties.stacksTo(1)
                        .durability(80)
                        .rarity(Rarity.UNCOMMON)
                        .repairable(Tags.Items.INGOTS_IRON)
        );
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        int damageValue = itemInHand.getDamageValue();
        int maxDamage = itemInHand.getMaxDamage();
        if (damageValue + 1 == maxDamage) {
            player.swing(hand);
            player.playSound(SoundEvents.ITEM_BREAK.value());
            player.getCooldowns().addCooldown(itemInHand, 20);
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        InteractionHand otherHand = hand.equals(InteractionHand.MAIN_HAND) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack other = player.getItemInHand(otherHand).copy();
        if (!other.isEmpty() && other.is(SnsItemTags.SQUEEZABLE)) {
            int count = other.getCount();
            itemInHand.set(DataComponentTypeRegistries.SQUEEZER_HOLDER, new SingleItemComponent(other.split(1)));
            if (player.hasInfiniteMaterials()) {
                other.setCount(count);
            }
            player.setItemInHand(otherHand, other);
        }
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity user) {
        return 20 * (itemStack.has(DataComponentTypeRegistries.SQUEEZER_HOLDER)?3 : 1);
    }

    @Override
    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity entity, int remainingTime) {
        ItemStack fruit = itemStack.getOrDefault(DataComponentTypeRegistries.SQUEEZER_HOLDER, SingleItemComponent.EMPTY).itemStack();
        itemStack.remove(DataComponentTypeRegistries.SQUEEZER_HOLDER);
        if (!fruit.isEmpty() && entity instanceof Player player) {
            player.addItem(fruit);
        }
        return super.releaseUsing(itemStack, level, entity, remainingTime);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int ticksRemaining) {
        if (!itemStack.has(DataComponentTypeRegistries.SQUEEZER_HOLDER)) {
            return;
        }
        if (shouldEmitParticlesAndSounds(ticksRemaining, itemStack, livingEntity)) {
            emitParticlesAndSounds(livingEntity, itemStack, 5);
        }
        if (ticksRemaining % 10 == 0) {
            RandomSource random = level.getRandom();
            float eatVolume = random.nextBoolean() ? 0.5F : 1.0F;
            float eatPitch = random.triangle(1.0F, 0.2F);
            SoundEvent consumeSound = SoundEvents.SLIME_BLOCK_HIT;
            livingEntity.playSound(consumeSound, eatVolume, eatPitch);
        }

        if (ticksRemaining % 3  == 0){
            HitResult pick = livingEntity.pick(livingEntity.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), 0.0F, false);
            if (pick instanceof BlockHitResult hitResult && !hitResult.getType().equals(HitResult.Type.MISS)) {
                Vec3 location = hitResult.getLocation();
                level.addAlwaysVisibleParticle(
                        ParticleTypes.DRIPPING_WATER,
                        location.x(),
                        location.y() + 0.2,
                        location.z(),
                        0,
                        0,
                        0
                );
            }
        }
        super.onUseTick(level, livingEntity, itemStack, ticksRemaining);
    }

    public void emitParticlesAndSounds(LivingEntity user, ItemStack itemStack, int particleCount) {
        SingleItemComponent itemComponent = itemStack.get(DataComponentTypeRegistries.SQUEEZER_HOLDER);
        if (itemComponent != null) {
            int invert = (user.getMainArm().equals(HumanoidArm.RIGHT) && user.getUsedItemHand().equals(InteractionHand.MAIN_HAND) ||
                    user.getMainArm().equals(HumanoidArm.LEFT) && user.getUsedItemHand().equals(InteractionHand.OFF_HAND))? -1: 1;
            ItemStack particleItem = itemComponent.itemStack();
            if (!particleItem.isEmpty()) {
                ItemParticleOption breakParticle = new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(particleItem));

                for (int i = 0; i < particleCount; i++) {
                    Vec3 d = new Vec3((user.getRandom().nextFloat() - 0.5) * 0.1, user.getRandom().nextFloat() * 0.1 + 0.1, 0.0);
                    d = d.xRot(-user.getXRot() * (float) (Math.PI / 180.0));
                    d = d.yRot(-user.getYRot() * (float) (Math.PI / 180.0));
                    double y1 = -user.getRandom().nextFloat() * 0.6 - 0.3;
                    Vec3 p = new Vec3((user.getRandom().nextFloat() - 0.5) * 0.3, y1, 0.6);
                    p = p.add(0.4 * invert, 0, 0);
                    p = p.xRot(-user.getXRot() * (float) (Math.PI / 180.0));
                    p = p.yRot(-user.getYRot() * (float) (Math.PI / 180.0));
                    p = p.add(user.getX(), user.getEyeY(), user.getZ());
                    user.level().addParticle(breakParticle, p.x, p.y, p.z, d.x, d.y + 0.05, d.z);
                }
            }
        }
    }

    public boolean shouldEmitParticlesAndSounds(int useItemRemainingTicks, ItemStack itemStack, LivingEntity livingEntity) {
        int consumeTicks = this.getUseDuration(itemStack, livingEntity);
        int itemUsedForTicks = consumeTicks - useItemRemainingTicks;
        int waitTicksBeforeUseEffects = 0;
        boolean isValidTime = itemUsedForTicks > waitTicksBeforeUseEffects;
        return isValidTime && useItemRemainingTicks % 4 == 0 && itemStack.has(DataComponentTypeRegistries.SQUEEZER_HOLDER);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        entity.stopUsingItem();
        if (entity instanceof Player player) {
            player.getCooldowns().addCooldown(player.getMainHandItem(), 5);
            player.getCooldowns().addCooldown(player.getOffhandItem(), 5);
        }
        ItemStack fruit = itemStack.getOrDefault(DataComponentTypeRegistries.SQUEEZER_HOLDER, SingleItemComponent.EMPTY).itemStack();
        itemStack.remove(DataComponentTypeRegistries.SQUEEZER_HOLDER);
        if (fruit.isEmpty()) {
            return super.finishUsingItem(itemStack, level, entity);
        }
        FluidStack juice = new FluidStack(
                fruit.is(SnsItemTags.LEMON)?
                        FluidRegistries.LEMONADE_SOURCE :
                        fruit.is(SnsItemTags.SWEET_FRUIT)?
                            FluidRegistries.SWEET_JUICE_SOURCE:
                            FluidRegistries.JUICE_SOURCE,
                250);
        juice.set(DataComponentTypeRegistries.FRUIT_DATA, new SingleItemComponent(fruit));
        HitResult pick = entity.pick(entity.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), 0.0F, false);
        if (pick instanceof BlockHitResult blockHitResult) {
            BlockEntity blockEntity = level.getBlockEntity(blockHitResult.getBlockPos());
            if (blockEntity instanceof ShakeBlockEntity be) {
                be.pourLiquid(juice, false);
            } else if (blockEntity instanceof SpiritBlockEntity be && be.getBlockState().getValue(SpiritBlock.COUNTS) == 1) {
                try (Transaction trx = Transaction.openRoot()){
                    int insert = be.getFluidHandler().insert(
                            0,
                            FluidResource.of(juice),
                            juice.amount(),
                            trx
                    );
                    if (insert != 0) {
                        trx.commit();
                    }
                }
            }
        }
        if (entity instanceof Player player){
            itemStack.hurtWithoutBreaking(1, player);
        }

        return super.finishUsingItem(itemStack, level, entity);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.BOW;
    }
}
