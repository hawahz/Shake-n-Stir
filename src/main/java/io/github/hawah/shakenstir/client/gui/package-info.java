package io.github.hawah.shakenstir.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

class MC{
    @Nullable
    public static Level getLevel() {
        return Minecraft.getInstance().level;
    }

    @Nullable
    public static Player getPlayer() {
        return Minecraft.getInstance().player;
    }
}