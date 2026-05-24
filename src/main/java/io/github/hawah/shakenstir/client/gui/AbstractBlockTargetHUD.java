package io.github.hawah.shakenstir.client.gui;

import io.github.hawah.shakenstir.client.ClientDataHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.gui.GuiLayer;

import javax.annotation.Nonnull;

public abstract class AbstractBlockTargetHUD implements GuiLayer {
    protected boolean isVisible() {
        if (ClientDataHolder.Picker.type().equals(HitResult.Type.MISS)) {
            return false;
        }
        return block().equals(ClientDataHolder.Picker.block().orElse(null));
    }

    @Nonnull
    protected abstract Block block();
}
