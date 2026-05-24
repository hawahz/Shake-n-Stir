package io.github.hawah.shakenstir.util;

import io.github.hawah.shakenstir.content.trigger.TriggerRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class AdvancementHooks {
    public static void onShakerOverturn(@Nullable Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            TriggerRegistries.SHAKER_OVERTURN.get().trigger(serverPlayer);
        }
    }
    public static void onShakeBubbleExplode(@Nullable Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            TriggerRegistries.SHAKE_BUBBLE_EXPLODE.get().trigger(serverPlayer);
        }
    }

    public static void onFirstDrunk(@Nullable Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            TriggerRegistries.FIRST_DRUNK.get().trigger(serverPlayer);
        }
    }

    public static void onDrunkHeavy(@Nullable Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            // TODO 需要补充: 添加额外触发条件，例如检查醉酒放大等级是否达到阈值
            TriggerRegistries.DRUNK_HEAVY.get().trigger(serverPlayer);
        }
    }

    public static void onFirstFallByDrunk(@Nullable Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            // TODO 需要补充: 添加额外触发条件，例如检查玩家是否因醉酒而摔倒
            TriggerRegistries.FIRST_FALL_BY_DRUNK.get().trigger(serverPlayer);
        }
    }

    public static void onFirstHitDueToLemon(@Nullable Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            // TODO 需要补充: 添加额外触发条件，例如检查攻击是否命中有效目标
            TriggerRegistries.FIRST_HIT_DUE_TO_LEMON.get().trigger(serverPlayer);
        }
    }

    public static void onDiedByDiscoveringParalysis(@Nullable Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            // TODO 需要补充: 添加额外触发条件，例如检查死亡伤害类型是否为麻痹伤害
            TriggerRegistries.DIED_BY_DISCOVERING_PARALYSIS.get().trigger(serverPlayer);
        }
    }

    public static void onProtectedByParalysis(@Nullable Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            // TODO 需要补充: 添加额外触发条件，例如检查吸收的伤害量是否达到阈值
            TriggerRegistries.PROTECTED_BY_PARALYSIS.get().trigger(serverPlayer);
        }
    }
}
