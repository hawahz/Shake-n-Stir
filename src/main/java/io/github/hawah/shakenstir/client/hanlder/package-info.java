package io.github.hawah.shakenstir.client.hanlder;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


class PACKAGE {
    public static Player getPlayer() {
        return Minecraft.getInstance().player;
    }

    public static ItemStack getItem() {
        return getPlayer().getMainHandItem();
    }

    public static Level level() {
        return Minecraft.getInstance().level;
    }
}