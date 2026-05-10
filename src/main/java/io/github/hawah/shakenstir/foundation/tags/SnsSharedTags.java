package io.github.hawah.shakenstir.foundation.tags;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public enum SnsSharedTags {
    SOUR(SnsFluidTags.SOUR, SnsItemTags.SOUR),
    BITTER(SnsFluidTags.BITTER, SnsItemTags.BITTER),
    SPIRIT(SnsFluidTags.SPIRIT, SnsItemTags.SPIRIT),
    SWEET(SnsFluidTags.SWEET, SnsItemTags.SWEET),
    BUBBLE(SnsFluidTags.BUBBLE_LIKE, SnsItemTags.BUBBLE_LIKE)
    ;
    public final TagKey<Fluid> fluidTag;
    public final TagKey<Item> itemTag;
    SnsSharedTags(TagKey<Fluid> fluidTag, TagKey<Item> itemTag) {
        this.fluidTag = fluidTag;
        this.itemTag = itemTag;
    }
}
