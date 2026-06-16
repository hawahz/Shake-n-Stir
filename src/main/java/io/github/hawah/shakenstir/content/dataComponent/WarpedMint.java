package io.github.hawah.shakenstir.content.dataComponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.item.MintItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WarpedMint {
    public static final Codec<WarpedMint> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.dispatchedMap(Codec.INT, _ -> Codec.INT)
                    .fieldOf("idx2Count").forGetter(warpedMint -> warpedMint.idx2Count)
    ).apply(inst, WarpedMint::new));

    public static final StreamCodec<ByteBuf, WarpedMint> STREAM_CODEC = new StreamCodec<ByteBuf, WarpedMint>() {
        @Override
        public WarpedMint decode(ByteBuf input) {
            int size = input.readInt();
            Map<Integer, Integer> map = new HashMap<>();
            for (int i = 0; i < size; i++) {
                int idx = input.readInt();
                int count = input.readInt();
                map.put(idx, count);
            }
            return new WarpedMint(map);
        }

        @Override
        public void encode(ByteBuf output, WarpedMint value) {
            int size = value.idx2Count.size();
            output.writeInt(size);
            for (Map.Entry<Integer, Integer> entry : value.idx2Count.entrySet()) {
                output.writeInt(entry.getKey());
                output.writeInt(entry.getValue());
            }
        }
    };

    public WarpedMint() {
    }
    public WarpedMint(Map<Integer, Integer> map) {
        idx2Count.putAll(map);
    }

    private final Map<Integer, Integer> idx2Count = new HashMap<>();
    public void merge(ItemStack itemStack) {
        int size = itemStack.getOrDefault(DataComponentTypeRegistries.MINT_SIZE, MintSizeComponent.of(-1)).size();
        if (size < 0 || !(itemStack.getItem() instanceof MintItem)) {
            return;
        }

        int count = itemStack.getCount();
        idx2Count.merge(size, count, (_, oldVal) -> oldVal + count);
    }
    public List<ItemStack> contents() {
        return idx2Count.entrySet()
                .stream()
                .map(e -> {
                    int size = e.getKey();
                    int count = e.getValue();
                    ItemStack stack = ItemRegistries.MINT.toStack(count);
                    stack.set(DataComponentTypeRegistries.MINT_SIZE, MintSizeComponent.of(size));
                    return stack;
                }).toList();

    }
    public boolean isEmpty() {
        flush();
        if (idx2Count.isEmpty()) {
            return true;
        }
        return idx2Count.values().stream().allMatch(num -> num == 0);
    }

    public int variety() {
        flush();
        return idx2Count.size();
    }

    public void flush() {
        idx2Count.values()
                .stream()
                .filter(itg -> idx2Count.get(itg)==0)
                .map(idx2Count::remove);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WarpedMint that)) return false;
        return Objects.equals(idx2Count, that.idx2Count);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idx2Count);
    }
}
