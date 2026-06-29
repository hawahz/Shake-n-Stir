package io.github.hawah.shakenstir.content.effect.event;

// TODO: 人工审查 - 2026-06-22

import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Misdirection（误导）效果的事件处理器，负责：
 * <ul>
 *     <li>挖掘时自动将主手物品替换为最适合目标方块的挖掘工具</li>
 *     <li>攻击时自动将主手物品替换为评分最高的武器</li>
 * </ul>
 */
@EventBusSubscriber
public final class MisdirectionEvents {

    /** 物品栏总槽位数（9 快捷栏 + 27 主物品栏） */
    private static final int INVENTORY_SIZE = 36;

    /** 低耐久阈值：低于此值给予大幅负分惩罚 */
    private static final int DURA_LOW_THRESHOLD = 10;
    /** 高耐久阈值：高于此值不再扣分 */
    private static final int DURA_HIGH_THRESHOLD = 50;
    /** 耐久惩罚基数 */
    private static final float DURA_PENALTY_BASE = 100f;
    /** 攻击伤害每点加分 */
    private static final float ATTACK_DAMAGE_BONUS = 10f;
    /** 装备时间每秒扣分 */
    private static final float EQUIP_TIME_PENALTY_PER_SEC = 1f;

    /** 自定义数据中装备时间戳的 key */
    private static final String TAG_EQUIP_TIME = "MisdirectionEquipTime";

    private MisdirectionEvents() {}

    // ==================== 挖掘行为 ====================

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity().level().isClientSide()) return;

        Player player = event.getEntity();
        if (!player.hasEffect(MobEffectRegistries.MISDIRECTION)) return;
        if (player.isCreative() || player.isSpectator()) return;
        Level level = event.getEntity().level();
        BlockState state = level.getBlockState(event.getPos());
        if (state.isAir() || state.getDestroySpeed(level, event.getPos()) < 0) return;

        // 步骤1：如果当前主手工具耐久过低，先将其移回物品栏
        tryMoveLowDurabilityItemOut(player);

        // 步骤2：寻找最适合挖掘该方块的快捷栏工具
        ItemStack mainHandItem = player.getMainHandItem();
        int bestSlot = -1;
        // 已持有最适合工具时，标记装备时间
        if (isCorrectOrBetterTool(mainHandItem, state)) {
            tryMarkEquipTime(player, mainHandItem, level.getGameTime());
        }

        // 扫描快捷栏（slot 0-8）
        bestSlot = getBestSlotRange(mainHandItem, state, player, bestSlot, 0, 9);

        if (bestSlot >= 0) {
            swapSlots(player, bestSlot, level);
            return;
        }

        // 扫描背包
        bestSlot = getBestSlotRange(mainHandItem, state, player, bestSlot, 9, INVENTORY_SIZE);

        if (bestSlot >= 0) {
            swapSlots(player, bestSlot, level);
        }
    }

    private static int getBestSlotRange(ItemStack mainHandItem, BlockState state, Player player, int bestSlot, int left, int right) {
        float bestSpeed = mainHandItem.getDestroySpeed(state);
        for (int slot = left; slot < right; slot++) {
            if (isCurrentMainHandSlot(player, slot)) continue;
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.isEmpty()) continue;
            if (!stack.isDamageableItem()) continue;

            int dura = stack.getMaxDamage() - stack.getDamageValue();
            if (dura < DURA_LOW_THRESHOLD) continue;

            float speed = stack.getDestroySpeed(state);
            boolean isCorrect = stack.isCorrectToolForDrops(state);

            boolean currentIsCorrect = bestSlot < 0
                    ? mainHandItem.isCorrectToolForDrops(state)
                    : player.getInventory().getItem(bestSlot).isCorrectToolForDrops(state);

            if (isCorrect && !currentIsCorrect) {
                bestSlot = slot;
                bestSpeed = speed;
            } else if (isCorrect == currentIsCorrect && speed > bestSpeed) {
                bestSlot = slot;
                bestSpeed = speed;
            }
        }
        return bestSlot;
    }

    private static boolean isCorrectOrBetterTool(ItemStack held, BlockState state) {
        if (held.isEmpty()) return false;
        return held.isCorrectToolForDrops(state) && held.getDestroySpeed(state) > 0;
    }

    // ==================== 攻击行为 ====================

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!player.hasEffect(MobEffectRegistries.MISDIRECTION)) return;
        if (player.isCreative() || player.isSpectator()) return;

        long gameTime = player.level().getGameTime();

        // 步骤1：当前武器耐久过低，尝试替换为空手或不可损坏物品
        ItemStack currentMainHand = player.getMainHandItem();
        if (isLowDurability(currentMainHand)) {
            int nonDamageableSlot = findNonDamageableItem(player);
            if (nonDamageableSlot >= 0) {
                swapSlots(player, nonDamageableSlot, player.level());
            } else if (!currentMainHand.isEmpty()) {
                tryMoveItemOutOfMainHand(player);
            }
            currentMainHand = player.getMainHandItem();
        }

        // 步骤2：使用评分函数寻找最佳武器
        int bestSlot = -1;
        float bestScore = scoreWeapon(player, currentMainHand, gameTime);

        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            if (isCurrentMainHandSlot(player, slot)) continue;
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.isEmpty()) continue;
            if (!stack.isDamageableItem() && bestSlot >= 0) continue;
            if (getAttackDamage(stack) <= 0) continue;

            int dura = stack.getMaxDamage() - stack.getDamageValue();
            if (dura < DURA_LOW_THRESHOLD) continue;

            float score = scoreWeapon(player, stack, gameTime);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = slot;
            }
        }

        if (bestSlot >= 0) {
            swapSlots(player, bestSlot, player.level());
        }
    }

    // ==================== 评分函数 ====================

    /**
     * 计算武器的综合评分。
     *
     * @param player          持有该效果的玩家
     * @param stack           候选武器
     * @param currentGameTime 当前游戏时间（tick）
     * @return 综合评分，越高越好
     */
    static float scoreWeapon(Player player, ItemStack stack, long currentGameTime) {
        if (stack.isEmpty()) return -1f;

        float score = 0f;

        // --- 耐久评分 ---
        if (stack.isDamageableItem()) {
            int currentDura = stack.getMaxDamage() - stack.getDamageValue();
            if (currentDura < DURA_LOW_THRESHOLD) {
                score -= DURA_PENALTY_BASE;
            } else if (currentDura < DURA_HIGH_THRESHOLD) {
                float ratio = (float) (DURA_HIGH_THRESHOLD - currentDura)
                        / (DURA_HIGH_THRESHOLD - DURA_LOW_THRESHOLD);
                score -= DURA_PENALTY_BASE * ratio;
            }
        }

        // --- 攻击伤害评分 ---
        float attackDamage = getAttackDamage(stack);
        score += attackDamage * ATTACK_DAMAGE_BONUS;

        // --- 装备时长评分 ---
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (!tag.isEmpty() && tag.contains(TAG_EQUIP_TIME)) {
            long equipTime = tag.getLong(TAG_EQUIP_TIME).orElse(0L);
            long elapsedTicks = currentGameTime - equipTime;
            float elapsedSeconds = elapsedTicks / 20f;
            score -= elapsedSeconds * EQUIP_TIME_PENALTY_PER_SEC;
        }

        return score;
    }

    // ==================== 装备时间追踪 ====================

    /** 尝试为物品标记当前装备时间（如果尚未标记）。 */
    private static void tryMarkEquipTime(Player player, ItemStack stack, long gameTime) {
        if (stack.isEmpty() || !stack.isDamageableItem()) return;
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, existing -> {
            if (existing.contains(TAG_EQUIP_TIME)) return existing;
            return existing.update(tag -> tag.putLong(TAG_EQUIP_TIME, gameTime));
        });
    }

    // ==================== 物品交换辅助方法 ====================

    /** 交换主手与目标槽位的物品，并播放反馈音效。 */
    private static void swapSlots(Player player, int targetSlot, Level level) {
        if (isCurrentMainHandSlot(player, targetSlot)) return;

        ItemStack mainHandStack = player.getMainHandItem();
        ItemStack targetStack = player.getInventory().getItem(targetSlot);

        // 标记被换上物品的装备时间
        tryMarkEquipTime(player, targetStack, level.getGameTime());

        // 执行交换
        player.getInventory().setItem(targetSlot, mainHandStack.copy());
        player.setItemInHand(InteractionHand.MAIN_HAND, targetStack.copy());

        // 播放音效反馈
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.3f, 2.0f);
        }
    }

    // ==================== 低耐久处理 ====================

    /** 检查物品是否为可损坏且耐久过低。 */
    private static boolean isLowDurability(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageableItem()) return false;
        int dura = stack.getMaxDamage() - stack.getDamageValue();
        return dura < DURA_LOW_THRESHOLD;
    }

    /** 尝试将当前主手低耐久物品移入物品栏空闲槽位。 */
    private static void tryMoveLowDurabilityItemOut(Player player) {
        if (!isLowDurability(player.getMainHandItem())) return;
        tryMoveItemOutOfMainHand(player);
    }

    /** 将主手物品移到物品栏中第一个空闲槽位，若无空闲槽位则保持不动。 */
    private static void tryMoveItemOutOfMainHand(Player player) {
        ItemStack mainHandStack = player.getMainHandItem();
        if (mainHandStack.isEmpty()) return;

        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            if (isCurrentMainHandSlot(player, slot)) continue;
            if (player.getInventory().getItem(slot).isEmpty()) {
                player.getInventory().setItem(slot, mainHandStack.copy());
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                return;
            }
        }
    }

    // ==================== 物品查找辅助方法 ====================

    /** 在物品栏中寻找不可损坏的物品（或空槽），跳过当前主手。 */
    private static int findNonDamageableItem(Player player) {
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            if (isCurrentMainHandSlot(player, slot)) continue;
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.isEmpty() || !stack.isDamageableItem()) {
                return slot;
            }
        }
        return -1;
    }

    /** 判断给定槽位是否为当前主手槽位（通过引用相等比较）。 */
    private static boolean isCurrentMainHandSlot(Player player, int slot) {
        return player.getMainHandItem() == player.getInventory().getItem(slot);
    }

    // ==================== 属性读取辅助 ====================

    /** 获取物品的基础攻击伤害。 */
    static float getAttackDamage(ItemStack stack) {
        if (stack.isEmpty()) return 0f;
        ItemAttributeModifiers attribs = stack.getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        float damage = 0f;
        for (ItemAttributeModifiers.Entry entry : attribs.modifiers()) {
            if (entry.attribute().is(Attributes.ATTACK_DAMAGE)) {
                damage += (float) entry.modifier().amount();
            }
        }
        return damage;
    }
}
