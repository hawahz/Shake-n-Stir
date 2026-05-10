package io.github.hawah.shakenstir.content.dataComponent;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ShakeProductDeferredName(String nameLike) {
    public static final ShakeProductDeferredName EMPTY = new ShakeProductDeferredName("");
    public static final Codec<ShakeProductDeferredName> CODEC = Codec.STRING.xmap(ShakeProductDeferredName::new, ShakeProductDeferredName::nameLike);
    public static final StreamCodec<ByteBuf, ShakeProductDeferredName> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(128), ShakeProductDeferredName::nameLike,
            ShakeProductDeferredName::new
    );

    public MutableComponent getName(List<FluidStack> fluidStacks, List<ItemStack> itemStacks) {
        String name = nameLike();
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
