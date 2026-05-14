package io.github.hawah.shakenstir.lib;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class StreamCodecUtil {
    public static final StreamCodec<ByteBuf, Vec3> VEC3 = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, Vec3::x,
            ByteBufCodecs.DOUBLE, Vec3::y,
            ByteBufCodecs.DOUBLE, Vec3::z,
            Vec3::new
    );

    public static final StreamCodec<ByteBuf, Double> DOUBLE = ByteBufCodecs.DOUBLE;

    public static final StreamCodec<ByteBuf, Quaternionf> QUATERNION = StreamCodec.composite(
            ByteBufCodecs.FLOAT, Quaternionf::x,
            ByteBufCodecs.FLOAT, Quaternionf::y,
            ByteBufCodecs.FLOAT, Quaternionf::z,
            ByteBufCodecs.FLOAT, Quaternionf::w,
            Quaternionf::new
    );


    public static <T extends Enum<T>> StreamCodec<ByteBuf, T> ofEnum(Class<T> enumClass) {
        return StreamCodec.composite(
                ByteBufCodecs.INT, Enum::ordinal,
                (ordinal) -> {
                    for (final T enumValue : enumClass.getEnumConstants()) {
                        if (enumValue.ordinal() == ordinal) {
                            return enumValue;
                        }
                    }
                    throw new IllegalArgumentException("Invalid ordinal");
                }
        );
    }
}
