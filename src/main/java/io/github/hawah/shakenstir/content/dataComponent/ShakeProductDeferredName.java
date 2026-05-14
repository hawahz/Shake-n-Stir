package io.github.hawah.shakenstir.content.dataComponent;

import com.mojang.serialization.Codec;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ShakeProductDeferredName(Component nameLike) {
    public static final ShakeProductDeferredName EMPTY = new ShakeProductDeferredName(Component.empty());
    public static final Codec<ShakeProductDeferredName> CODEC = ComponentSerialization.CODEC
            .xmap(ShakeProductDeferredName::new, ShakeProductDeferredName::nameLike);
    public static final StreamCodec<RegistryFriendlyByteBuf, ShakeProductDeferredName> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, ShakeProductDeferredName::nameLike,
            ShakeProductDeferredName::new
    );

    public ShakeProductDeferredName(String nameLike) {
        this(Component.translatable(nameLike));
    }

    public ShakeProductDeferredName(LangData nameLike) {
        this(nameLike.get());
    }

    public MutableComponent getName(List<FluidStack> fluidStacks, List<ItemStack> itemStacks) {
        String name = nameLike().getString();
        if (!fluidStacks.isEmpty())
            name = name.replace("{Fluid}", fluidStacks.stream().max(Comparator.comparingInt(FluidStack::getAmount)).get().getHoverName().getString());
        if (!itemStacks.isEmpty())
            name = name.replace("{Ingredient}", itemStacks.getFirst().getHoverName().getString());
        Pattern bracesPattern = Pattern.compile("\\{[^}]*}");
        Matcher matcher = bracesPattern.matcher(name);
        name = matcher.replaceAll("").trim().replaceAll("\\s+", " ");
        return Component.literal(name);
    }
}
