package io.github.hawah.shakenstir.client.render.general;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class GlasswareDecorations {
    public static Map<Item, Identifier> maps = new HashMap<>();

    static {
        maps.put(Items.POPPY, ShakenStir.asResource("poppy_deco"));
    }
}
