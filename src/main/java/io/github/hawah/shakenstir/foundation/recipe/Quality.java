package io.github.hawah.shakenstir.foundation.recipe;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.Codec;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum Quality implements StringRepresentable, TooltipProvider {
    MASTERPIECE ("Masterpiece"  , LangData.TOOLTIP_MASTERPIECE ),
    SUPERIOR    ("Superior"     , LangData.TOOLTIP_SUPERIOR    ),
    EXCELLENT   ("Excellent"    , LangData.TOOLTIP_EXCELLENT   ),
    GOOD        ("Good"         , LangData.TOOLTIP_GOOD        ),
    AVERAGE     ("Average"      , LangData.TOOLTIP_AVERAGE     ),
    POOR        ("Poor"         , LangData.TOOLTIP_POOR        ),
    BAD         ("Bad"          , LangData.TOOLTIP_BAD         ),
    TERRIBLE    ("Terrible"     , LangData.TOOLTIP_TERRIBLE    ),
    DISASTER    ("Disaster"     , LangData.TOOLTIP_DISASTER    );


    public final String name;
    public final LangData tooltip;
    public static final Codec<Quality> CODEC = StringRepresentable.fromEnum(Quality::values);
    public static final StreamCodec<ByteBuf, Quality> STREAM_CODEC = ByteBufCodecs.idMapper(Quality::getQuality, Quality::ordinal);


    Quality(String name, LangData tooltip) {
        this.name = name;
        this.tooltip = tooltip;
    }

    public static Quality getQuality(int quality) {
        return Quality.values()[quality];
    }
    public int toSignedIndex() {
        int ordinal = ordinal();
        return -(ordinal - AVERAGE.ordinal());
    }

    public static Quality calculate(int shakeFailedTimes, float iceMeltProcess, int iceCubeCounts, int shakeAdditionTimes) {
        float penalty = 0;

        penalty += shakeFailedTimes * 2.5f;

        if (iceCubeCounts > 0 && iceMeltProcess >= 0.5f) {
            float excessMelt = iceMeltProcess - 0.5f;
            penalty += excessMelt * 2.0f * iceCubeCounts;
        }

        if (penalty == 0) {
            return shakeAdditionTimes > iceCubeCounts * 3 ? MASTERPIECE : SUPERIOR;
        }

        if (penalty < 2) return EXCELLENT;
        if (penalty < 4) return GOOD;
        if (penalty < 6) return AVERAGE;
        if (penalty < 8) return POOR;
        if (penalty < 10) return BAD;
        if (penalty < 12) return TERRIBLE;
        return DISASTER;
    }

    public Component getTooltip() {
        return tooltip.get();
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        consumer.accept(getTooltip());
    }
}
