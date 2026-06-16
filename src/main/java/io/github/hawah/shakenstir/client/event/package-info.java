@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
package io.github.hawah.shakenstir.client.event;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

class MC {
    static @Nullable ClientLevel getLevel() {
        return Minecraft.getInstance().level;
    }

    static @Nullable LocalPlayer getPlayer() {
        return Minecraft.getInstance().player;
    }
}