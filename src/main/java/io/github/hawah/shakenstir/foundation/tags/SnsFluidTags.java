package io.github.hawah.shakenstir.foundation.tags;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class SnsFluidTags {

    public static final TagKey<Fluid> GIN = sns("gins");
    public static final TagKey<Fluid> VODKA = sns("vodkas");
    public static final TagKey<Fluid> RUM = sns("rums");
    public static final TagKey<Fluid> TEQUILA = sns("tequilas");
    public static final TagKey<Fluid> WHISKY = sns("whiskys");
    public static final TagKey<Fluid> BRANDY = sns("brandys");

    public static final TagKey<Fluid> SPIRIT = common("spirits");

    public static final TagKey<Fluid> BUBBLE_LIKE = common("bubbles");

    public static final TagKey<Fluid> SWEET = common("sweets");

    public static final TagKey<Fluid> SOUR = common("sours");

    public static final TagKey<Fluid> BITTER = common("bitters");

    private static TagKey<Fluid> common(String name) {
        return FluidTags.create(Identifier.fromNamespaceAndPath("c", name));
    }

    private static TagKey<Fluid> sns(String name) {
        return FluidTags.create(ShakenStir.asResource(name));
    }
}
