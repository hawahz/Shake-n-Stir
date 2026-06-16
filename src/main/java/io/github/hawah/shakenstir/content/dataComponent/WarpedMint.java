package io.github.hawah.shakenstir.content.dataComponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.item.MintItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WarpedMint {
    public static final Codec<WarpedMint> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.listOf().fieldOf("counts").forGetter(w -> List.of(w.counts[0], w.counts[1], w.counts[2]))
    ).apply(inst, list -> {
        int[] arr = new int[3];
        for (int i = 0; i < 3 && i < list.size(); i++) arr[i] = list.get(i);
        return new WarpedMint(arr);
    }));

    public static final StreamCodec<ByteBuf, WarpedMint> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public WarpedMint decode(ByteBuf input) {
            int[] counts = new int[3];
            for (int i = 0; i < 3; i++) counts[i] = input.readInt();
            return new WarpedMint(counts);
        }

        @Override
        public void encode(ByteBuf output, WarpedMint value) {
            for (int i = 0; i < 3; i++) output.writeInt(value.counts[i]);
        }
    };

    public WarpedMint() {
    }

    public WarpedMint(int[] counts) {
        System.arraycopy(counts, 0, this.counts, 0, 3);
    }

    private final int[] counts = new int[3];

    public void merge(ItemStack itemStack) {
        int size = itemStack.getOrDefault(DataComponentTypeRegistries.MINT_SIZE, MintSizeComponent.of(-1)).size();
        if (size < 0 || size >= 3 || !(itemStack.getItem() instanceof MintItem)) {
            return;
        }
        counts[size] += itemStack.getCount();
    }

    public boolean merge(WarpedMint other) {
        if (other.isEmpty()) {
            return true;
        }
        for (int i = 0; i < other.counts.length; i++) {
            counts[i] += other.counts[i];
            other.counts[i] = 0;
        }
        return true;
    }

    public int[] getCounts() {
        return Arrays.copyOf(counts, counts.length);
    }

    public List<ItemStack> contents() {
        List<ItemStack> list = new ArrayList<>();
        for (int size = 0; size < 3; size++) {
            if (counts[size] > 0) {
                ItemStack stack = ItemRegistries.MINT.toStack(counts[size]);
                stack.set(DataComponentTypeRegistries.MINT_SIZE, MintSizeComponent.of(size));
                list.add(stack);
            }
        }
        return list;
    }

    public boolean isEmpty() {
        return counts[0] == 0 && counts[1] == 0 && counts[2] == 0;
    }

    public int variety() {
        int v = 0;
        for (int i = 0; i < 3; i++) if (counts[i] > 0) v++;
        return v;
    }

    public WarpedMint copy(){
        return new WarpedMint(Arrays.stream(counts).toArray());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WarpedMint that)) return false;
        return Arrays.equals(counts, that.counts);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(counts);
    }
}
