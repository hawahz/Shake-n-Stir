package io.github.hawah.shakenstir.foundation.block;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public interface IPlacePriority {
    boolean isPriority(PlayerInteractEvent.RightClickBlock event);
}
