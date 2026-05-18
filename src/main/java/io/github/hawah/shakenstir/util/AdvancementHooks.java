package io.github.hawah.shakenstir.util;

import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class AdvancementHooks {
    public static void onShakeBubbleExplode(@Nullable Player player) {
        if (player == null) {
            return;
        }
    }

    public static void onFirstDrunk(@Nullable Player player) {
        if (player == null) {
            return;
        }
    }

    public static void onDrunkHeavy(@Nullable Player player) {
        if (player == null) {
            return;
        }
    }

    public static void onFirstFallByDrunk(@Nullable Player player) {
        if (player == null) {
            return;
        }
    }

    public static void onFirstHitDueToLemon(@Nullable Player player) {
        if (player == null) {
            return;
        }
    }

    public static void onDiedByDiscoveringParalysis(@Nullable Player player) {
        if (player == null) {
            return;
        }
    }

    public static void onProtectedByParalysis(@Nullable Player player) {
        if (player == null) {
            return;
        }
    }
}
