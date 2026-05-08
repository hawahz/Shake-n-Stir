package io.github.hawah.shakenstir.util;

import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class AdvancementHooks {
    public static void onShakeBubbleExplode(@Nullable Player player) {
        if (player == null) {
            return;
        }
    }
}
