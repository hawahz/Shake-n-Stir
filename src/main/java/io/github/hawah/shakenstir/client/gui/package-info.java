package io.github.hawah.shakenstir.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

class MC{
    @Nullable
    public static Level getLevel() {
        return Minecraft.getInstance().level;
    }
}