package io.github.hawah.shakenstir.foundation.tags;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class SnsFluidTags {

    public static final TagKey<Fluid> GIN = TagKey.create(
            Registries.FLUID,
            ShakenStir.asResource("gin")
    );
    public static final TagKey<Fluid> VODKA = TagKey.create(
            Registries.FLUID,
            ShakenStir.asResource("vodka")
    );
    public static final TagKey<Fluid> RUM = TagKey.create(
            Registries.FLUID,
            ShakenStir.asResource("rum")
    );
    public static final TagKey<Fluid> TEQUILA = TagKey.create(
            Registries.FLUID,
            ShakenStir.asResource("tequila")
    );
    public static final TagKey<Fluid> WHISKY = TagKey.create(
            Registries.FLUID,
            ShakenStir.asResource("whisky")
    );
    public static final TagKey<Fluid> BRANDY = TagKey.create(
            Registries.FLUID,
            ShakenStir.asResource("brandy")
    );

    public static final TagKey<Fluid> SPIRIT = TagKey.create(
            Registries.FLUID,
            ShakenStir.asResource("spirit_tag")
    );

    public static final TagKey<Fluid> BUBBLE_LIKE = TagKey.create(
            Registries.FLUID,
            ShakenStir.asResource("bubble_like")
    );

    public static final TagKey<Fluid> SWEET = TagKey.create(
            Registries.FLUID,
            ShakenStir.asResource("sweet")
    );

    public static final TagKey<Fluid> SOUR = TagKey.create(
            Registries.FLUID,
            ShakenStir.asResource("sour")
    );

    public static final TagKey<Fluid> BITTER = TagKey.create(
            Registries.FLUID,
            ShakenStir.asResource("bitter")
    );
}
