package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.content.block.BlockRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Set;

public class ModLootTableProvider  extends BlockLootSubProvider {
    protected ModLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(
                BlockRegistries.BAR_MENU_BLOCK.get(),
                BlockRegistries.BAR_COUNTER_BLOCK.get(),
                BlockRegistries.CABINET.get()
        );
    }

    @Override
    protected void generate() {
        this.dropSelf(BlockRegistries.BAR_MENU_BLOCK.get());
        this.dropSelf(BlockRegistries.BAR_COUNTER_BLOCK.get());
        this.dropSelf(BlockRegistries.CABINET.get());
    }

    protected LootTable.Builder createBarMenuDrop(Block barMenu) {
        return LootTable.lootTable()
                .withPool(
                        this.applyExplosionCondition(
                                barMenu,
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(barMenu)
                                                        .apply(
                                                                CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                                                                        .include(DataComponents.CUSTOM_NAME)
                                                        )
                                        )
                        )
                );
    }
}
