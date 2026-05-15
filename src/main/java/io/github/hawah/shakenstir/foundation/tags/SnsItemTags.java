package io.github.hawah.shakenstir.foundation.tags;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class SnsItemTags {
    public static final TagKey<Item> SPIRIT = common("spirits");

    public static final TagKey<Item> SOUR = common("sours");

    public static final TagKey<Item> SWEET = common("sweets");

    public static final TagKey<Item> BITTER = common("bitters");

    public static final TagKey<Item> BUBBLE_LIKE = common("bubbles");

    public static final TagKey<Item> SHAKE_PLACABLE = sns("shake_placables");

    public static final TagKey<Item> GLASSWARE = sns("glasswares");

    public static final TagKey<Item> DRINK_DECORATION = sns("drink_decorations");
    public static final TagKey<Item> BLOCK_LIKE_DRINK_DECORATION = sns("block_like_drink_decorations");
    public static final TagKey<Item> ITEM_LIKE_DRINK_DECORATION = sns("item_like_drink_decorations");

    private static TagKey<Item> common(String name) {
        return ItemTags.create(Identifier.fromNamespaceAndPath("c", name));
    }

    private static TagKey<Item> sns(String name) {
        return ItemTags.create(ShakenStir.asResource(name));
    }
}
