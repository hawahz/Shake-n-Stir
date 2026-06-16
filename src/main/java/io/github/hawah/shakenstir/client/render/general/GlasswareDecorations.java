package io.github.hawah.shakenstir.client.render.general;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.MintSizeComponent;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class GlasswareDecorations {
    public static Map<Predicate<ItemStack>, Identifier> maps = new HashMap<>();

    static {
        maps.put(is(Items.POPPY), ShakenStir.asResource("poppy_deco"));
        maps.put(component(DataComponentTypeRegistries.MINT_SIZE, new MintSizeComponent(1)), ShakenStir.asResource("mint_deco1"));
    }

    public static Predicate<ItemStack> is(ItemLike item) {
        return itemStack -> itemStack.is(item.asItem());
    }

    public static <T> Predicate<ItemStack> component(DataComponentType<T> componentType, @Nonnull T v) {
        return itemStack -> v.equals(itemStack.get(componentType));
    }
}
