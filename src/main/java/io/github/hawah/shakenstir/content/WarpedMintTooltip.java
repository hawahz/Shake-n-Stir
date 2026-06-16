package io.github.hawah.shakenstir.content;

import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.WarpedMint;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record WarpedMintTooltip(WarpedMint warpedMint, int index) implements TooltipComponent {
    public static Optional<TooltipComponent> of(ItemStack itemStack) {
        return Optional.of(new WarpedMintTooltip(itemStack.get(DataComponentTypeRegistries.WARPED_MINT), itemStack.getOrDefault(DataComponentTypeRegistries.SELECT_MINT, -1)));
    }
}
