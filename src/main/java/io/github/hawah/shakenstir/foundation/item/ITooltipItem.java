package io.github.hawah.shakenstir.foundation.item;

import net.neoforged.neoforge.event.AddAttributeTooltipsEvent;

public interface ITooltipItem {

    void appendHoverText(AddAttributeTooltipsEvent event);
}
