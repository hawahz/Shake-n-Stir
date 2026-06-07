package io.github.hawah.shakenstir.content.dataComponent;

import com.mojang.serialization.Codec;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import io.github.hawah.shakenstir.content.recipe.datapack.spirit.Spirits;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.function.Consumer;

public record SpiritContent(FluidStack fluidStack) implements TooltipProvider {
    public static final Codec<SpiritContent> CODEC = FluidStack.OPTIONAL_CODEC.xmap(SpiritContent::new, SpiritContent::fluidStack);
    public static final StreamCodec<RegistryFriendlyByteBuf, SpiritContent> STREAM_CODEC = StreamCodec.composite(
            FluidStack.OPTIONAL_STREAM_CODEC,
            SpiritContent::fluidStack,
            SpiritContent::new
    );
    public static final SpiritContent EMPTY = new SpiritContent(FluidStack.EMPTY);

    public boolean isEmpty() {
        return fluidStack.isEmpty();
    }

    @Override
    public FluidStack fluidStack() {
        return fluidStack.copy();
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        if (fluidStack().isEmpty()) {
            consumer.accept(LangData.TOOLTIP_SPIRIT_EMPTY.get());
        } else {
            consumer.accept(LangData.TOOLTIP_SPIRIT_CONTENT.get(fluidStack().getHoverName()));
            consumer.accept(LangData.TOOLTIP_SPIRIT_VOLUME.get(fluidStack().getAmount()));
            Spirits.getBuiltIn(fluidStack().typeHolder()).ifPresent(
                    spirit -> {
                        consumer.accept(CommonComponents.EMPTY);
                        consumer.accept(LangData.TOOLTIP_WHEN_SPIRIT_AS_BASE.get());
                        MobEffect positive = spirit.effectData().positive().value();
                        MobEffect negative = spirit.effectData().negative().value();
                        MutableComponent tooltip = LangData.TOOLTIP_SPIRIT_POSITIVE_N_NEGATIVE.get(
                                Component.empty().append(positive.getDisplayName()).withStyle(positive.getCategory().getTooltipFormatting()),
                                Component.empty().append(negative.getDisplayName()).withStyle(negative.getCategory().getTooltipFormatting())
                        );
                        consumer.accept(tooltip);

                    }
            );
        }
    }
}
