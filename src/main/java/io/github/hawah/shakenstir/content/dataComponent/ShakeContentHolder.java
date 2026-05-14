package io.github.hawah.shakenstir.content.dataComponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public record ShakeContentHolder(
        List<FluidStack> fluidStacks,
        List<ItemStack> itemStacks,
        int fluidMaxVolume,
        int itemMaxCount
) implements IFluidDataHolder, IItemDataHolder{
    public static final ShakeContentHolder EMPTY = new ShakeContentHolder(NonNullList.of(FluidStack.EMPTY), NonNullList.of(ItemStack.EMPTY), 1000, 4);
    public static ShakeContentHolder of(List<FluidStack> fluidStacks, List<ItemStack> itemStacks) {
        return new ShakeContentHolder(fluidStacks, itemStacks, 1000, 4);
    }

    public static final Codec<ShakeContentHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FluidStack.OPTIONAL_CODEC.listOf().fieldOf("fluidStacks").forGetter(ShakeContentHolder::fluidStacks),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("itemStacks").forGetter(ShakeContentHolder::itemStacks),
            Codec.INT.fieldOf("fluidMaxVolume").forGetter(ShakeContentHolder::fluidMaxVolume),
            Codec.INT.fieldOf("itemMaxCount").forGetter(ShakeContentHolder::itemMaxCount)
    ).apply(instance, ShakeContentHolder::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShakeContentHolder> STREAM_CODEC = StreamCodec.composite(
            FluidStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), ShakeContentHolder::fluidStacks,
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), ShakeContentHolder::itemStacks,
            ByteBufCodecs.INT, ShakeContentHolder::fluidMaxVolume,
            ByteBufCodecs.INT, ShakeContentHolder::itemMaxCount,
            ShakeContentHolder::new
    );

    public void validate() {
        fluidStacks.removeIf(FluidStack::isEmpty);
        itemStacks.removeIf(ItemStack::isEmpty);
    }

    public int fluidVolume() {
        return fluidStacks.stream().mapToInt(FluidStack::getAmount).sum();
    }

    public int fluidVariety() {
        return fluidStacks.size();
    }

    public int itemCount() {
        return itemStacks.stream().mapToInt(ItemStack::getCount).sum();
    }

    public boolean insertFluid(FluidStack fluidStack) {
        int rest = fluidMaxVolume - fluidVolume();
        if (fluidStack.isEmpty() || rest <= 0) {
            return false;
        }


        int valid = Math.min(rest, fluidStack.getAmount());
        for (FluidStack stack : fluidStacks) {
            if (stack.is(fluidStack.getFluid())) {
                stack.grow(valid);
                fluidStack.shrink(valid);
                return true;
            }
        }

        fluidStacks.add(fluidStack.split(valid));

        return true;
    }

    public boolean insertItem(ItemStack itemStack) {
        int rest = itemMaxCount - itemCount();
        if (itemStack.isEmpty() || rest <= 0) {
            return false;
        }
        itemStacks.add(itemStack.split(1));
        return true;
    }

    public class Fluid {
        public boolean isEmpty() {
            validate();
            return fluidVolume() == 0;
        }

        public int volume() {
            validate();
            return fluidVolume();
        }

        public boolean insert(FluidStack fluidStack) {
            validate();
            return insertFluid(fluidStack);
        }
    }

    public class Item {
        public boolean isEmpty() {
            validate();
            return itemCount() == 0;
        }

        public int count() {
            validate();
            return itemCount();
        }

        public boolean insert(ItemStack itemStack) {
            validate();
            return insertItem(itemStack);
        }
    }
}
