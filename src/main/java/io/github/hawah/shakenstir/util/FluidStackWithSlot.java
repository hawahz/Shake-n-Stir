package io.github.hawah.shakenstir.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public record FluidStackWithSlot(int slot, FluidStack stack) {
    public static final Codec<FluidStackWithSlot> CODEC = RecordCodecBuilder.create(
            i -> i.group(
                            ExtraCodecs.UNSIGNED_BYTE.fieldOf("Slot").orElse(0).forGetter(FluidStackWithSlot::slot),
                            FluidStack.MAP_CODEC.forGetter(FluidStackWithSlot::stack)
                    )
                    .apply(i, FluidStackWithSlot::new)
    );

    public boolean isValidInContainer(int containerSize) {
        return this.slot >= 0 && this.slot < containerSize;
    }
}