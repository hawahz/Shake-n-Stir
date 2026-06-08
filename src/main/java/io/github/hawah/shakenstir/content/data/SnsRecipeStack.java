package io.github.hawah.shakenstir.content.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public final class SnsRecipeStack {
    public static final Codec<SnsRecipeStack> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            SnsRecipeHolder.CODEC.fieldOf("holder").forGetter(SnsRecipeStack::holder),
            Codec.INT.fieldOf("amount").forGetter(SnsRecipeStack::amount)
    ).apply(inst, SnsRecipeStack::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SnsRecipeStack> STREAM_CODEC = StreamCodec.composite(
            SnsRecipeHolder.STREAM_CODEC, SnsRecipeStack::holder,
            ByteBufCodecs.INT, SnsRecipeStack::amount,
            SnsRecipeStack::new
    );
    private final SnsRecipeHolder holder;
    private int amount;

    public SnsRecipeStack(SnsRecipeHolder holder, int amount) {
        this.holder = holder;
        this.amount = amount;
    }

    public SnsRecipeStack shrink(int v) {
        amount -= v;
        amount = Math.max(0, amount);
        return this;
    }

    public SnsRecipeStack shrink() {
        return shrink(1);
    }

    public boolean isEmpty() {
        return amount == 0;
    }

    public SnsRecipeHolder holder() {
        return holder;
    }

    public int amount() {
        return amount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SnsRecipeStack) obj;
        return Objects.equals(this.holder, that.holder) &&
                this.amount == that.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(holder, amount);
    }

    @Override
    public String toString() {
        return "SnsRecipeStack[" +
                "holder=" + holder + ", " +
                "amount=" + amount + ']';
    }

}
