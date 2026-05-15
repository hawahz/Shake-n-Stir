package io.github.hawah.shakenstir.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.joml.Quaternionf;
import org.joml.Vector2f;

public class SerializeHelper {

    public static final Codec<Vector2f> VEC2F_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.fieldOf("x").forGetter(Vector2f::x),
            Codec.FLOAT.fieldOf("y").forGetter(Vector2f::y)
    ).apply(inst, Vector2f::new));

    public static final StreamCodec<ByteBuf, Vector2f> VEC2F_STREAM_CODEC  = StreamCodec.composite(
            ByteBufCodecs.FLOAT, Vector2f::x,
            ByteBufCodecs.FLOAT, Vector2f::y,
            Vector2f::new
    );

    public static void saveVector2f(ValueOutput output, Vector2f vec) {
        float x = vec.x();
        float y = vec.y();
        int data = (int) (x * 1000) | ((int) (y * 1000) << 16);
        output.putInt("Vector2f", data);
    }

    public static void loadVector2f(ValueInput input, Vector2f dst) {
        int data = input.getInt("Vector2f").orElse(0);
        float x = (data & 0xFFFF) / 1000f;
        float y = (data >> 16 & 0xFFFF) / 1000f;
        dst.set(x, y);
    }

    public static final Codec<PatchedDataComponentMap> DATA_COMPONENT_MAP_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            DataComponentPatch.CODEC.fieldOf("data").forGetter(PatchedDataComponentMap::asPatch)
    ).apply(inst, patch ->  PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch)));

    public static final StreamCodec<RegistryFriendlyByteBuf, PatchedDataComponentMap> DATA_COMPONENT_MAP_STREAM_CODEC = StreamCodec.composite(
            DataComponentPatch.STREAM_CODEC, PatchedDataComponentMap::asPatch,
            (patch) -> PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch)
    );

    public static <T extends Enum<T>> Codec<T> ofEnum(Class<T> enumClass) {
        return Codec.INT.xmap(
                ordinal -> enumClass.getEnumConstants()[Math.min(ordinal, enumClass.getEnumConstants().length - 1)],
                Enum::ordinal
        );
    }

    public static final Codec<Quaternionf> QUATERNIONF_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.fieldOf("x").forGetter(Quaternionf::x),
            Codec.FLOAT.fieldOf("y").forGetter(Quaternionf::y),
            Codec.FLOAT.fieldOf("z").forGetter(Quaternionf::z),
            Codec.FLOAT.fieldOf("w").forGetter(Quaternionf::w)
    ).apply(inst, Quaternionf::new));
}
