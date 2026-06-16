package io.github.hawah.shakenstir.content.dataComponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.function.Consumer;

public record MintSizeComponent(int size) implements TooltipProvider {
    public static final Codec<MintSizeComponent> CODEC = RecordCodecBuilder.create(inst->inst.group(
            Codec.INT.fieldOf("size").forGetter(MintSizeComponent::size)
    ).apply(inst, MintSizeComponent::new));
    public static final StreamCodec<ByteBuf, MintSizeComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, MintSizeComponent::size,
            MintSizeComponent::new 
    );
    public static MintSizeComponent of(int size) {
        return new MintSizeComponent(size);
    }
    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        consumer.accept(switch (size()) {
            case 0 -> LangData.TOOLTIP_MINT_SIZE_SMALL.get();
            case 1 -> LangData.TOOLTIP_MINT_SIZE_MEDIUM.get();
            case 2 -> LangData.TOOLTIP_MINT_SIZE_LARGE.get();
            default -> Component.literal("UNK").withStyle(ChatFormatting.DARK_RED);
        });
    }
}
