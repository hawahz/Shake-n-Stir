@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
package io.github.hawah.shakenstir.client.hanlder;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;


class MC {

    @Nullable
    public static LocalPlayer getPlayer() {
        return Minecraft.getInstance().player;
    }

    public static ItemStack getItem() {
        return getPlayer().getMainHandItem();
    }

    @Nullable
    public static Level level() {
        return Minecraft.getInstance().level;
    }

    public static Minecraft mc() {
        return Minecraft.getInstance();
    }
}