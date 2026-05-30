package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.content.block.BlockRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Set;

public class ModLootTableProvider  extends BlockLootSubProvider {
    protected ModLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(BlockRegistries.BAR_MENU_BLOCK.get());
    }

    @Override
    protected void generate() {
        this.dropSelf(BlockRegistries.BAR_MENU_BLOCK.get());
    }
}
