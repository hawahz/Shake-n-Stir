package io.github.hawah.shakenstir.foundation.tags;


import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class SnsBlockTags {
    public static final TagKey<Block> BLOCKING_FLUID = common("blocking_fluid");
    public static final TagKey<Block> BAR_AREA_IGNORED = common("bar_area_ignored");


    private static TagKey<Block> common(String name) {
        return BlockTags.create(Identifier.fromNamespaceAndPath("c", name));
    }
}
