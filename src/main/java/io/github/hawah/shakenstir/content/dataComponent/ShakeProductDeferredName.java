package io.github.hawah.shakenstir.content.dataComponent;

import com.mojang.serialization.Codec;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Comparator;
import java.util.List;

public record ShakeProductDeferredName(String nameLike) {
    public static final ShakeProductDeferredName EMPTY = new ShakeProductDeferredName(Component.empty());
    public static final Codec<ShakeProductDeferredName> CODEC = Codec.STRING
            .xmap(ShakeProductDeferredName::new, ShakeProductDeferredName::nameLike);
    public static final StreamCodec<RegistryFriendlyByteBuf, ShakeProductDeferredName> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(128), ShakeProductDeferredName::nameLike,
            ShakeProductDeferredName::new
    );

//    public ShakeProductDeferredName(String nameLike) {
//        this(Component.translatable(nameLike));
//    }

    public ShakeProductDeferredName(LangData nameLike) {
        this(nameLike.key);
    }

    public ShakeProductDeferredName(Component nameLike) {
        this(nameLike.toString());
    }

    public MutableComponent getName(List<FluidStack> fluidStacks, List<ItemStack> itemStacks) {
        MutableComponent name = Component.translatable(
                nameLike,
                fluidStacks.stream().max(Comparator.comparingInt(FluidStack::getAmount)).get().getHoverName(),
                itemStacks.getFirst().getHoverName()
        );
//        if (!fluidStacks.isEmpty())
//            name = name.replace("{Fluid}", fluidStacks.stream().max(Comparator.comparingInt(FluidStack::getAmount)).get().getHoverName().getString());
//        if (!itemStacks.isEmpty())
//            name = name.replace("{Ingredient}", itemStacks.getFirst().getHoverName().getString());
//        Pattern bracesPattern = Pattern.compile("\\{[^}]*}");
//        Matcher matcher = bracesPattern.matcher(name);
//        name = matcher.replaceAll("").trim().replaceAll("\\s+", " ");
        return name;
    }
}
