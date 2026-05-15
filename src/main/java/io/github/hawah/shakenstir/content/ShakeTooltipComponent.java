package io.github.hawah.shakenstir.content;

import io.github.hawah.shakenstir.content.dataComponent.ShakeContentHolder;
import io.github.hawah.shakenstir.foundation.utils.ShakeUtil;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record ShakeTooltipComponent(ShakeContentHolder contentHolder, int iceCounts, boolean canLookThrough) implements TooltipComponent {
    public static Optional<TooltipComponent> of(ItemStack itemStack) {
        ShakeContentHolder shakeContentHolder = ShakeUtil.get(itemStack);
        return Optional.of(new ShakeTooltipComponent(shakeContentHolder, ShakeUtil.getIceCount(itemStack), !ShakeUtil.hasCup(itemStack)));
    }
}
