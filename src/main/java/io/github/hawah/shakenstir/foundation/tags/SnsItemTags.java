package io.github.hawah.shakenstir.foundation.tags;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class SnsItemTags {
    public static final TagKey<Item> SPIRIT = TagKey.create(
            Registries.ITEM,
            ShakenStir.asResource("spirit_item_tag")
    );

    public static final TagKey<Item> SOUR = TagKey.create(
            Registries.ITEM,
            ShakenStir.asResource("sour_item_tag")
    );

    public static final TagKey<Item> SWEET = TagKey.create(
            Registries.ITEM,
            ShakenStir.asResource("sweet_item_tag")
    );

    public static final TagKey<Item> BITTER = TagKey.create(
            Registries.ITEM,
            ShakenStir.asResource("bitter_item_tag")
    );

    public static final TagKey<Item> BUBBLE_LIKE = TagKey.create(
            Registries.ITEM,
            ShakenStir.asResource("bubble_like_item_tag")
    );

    public static final TagKey<Item> SHAKE_PLACABLE = TagKey.create(
            Registries.ITEM,
            ShakenStir.asResource("shake_placable_item_tag")
    );

    public static final TagKey<Item> GLASSWARE = TagKey.create(
            Registries.ITEM,
            ShakenStir.asResource("glassware_item_tag")
    );
}
