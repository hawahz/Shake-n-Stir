package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.block.MintPlantBlock;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.MintSizeComponent;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Set;

public class ModBlockLoot extends BlockLootSubProvider {
    protected ModBlockLoot(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(
                BlockRegistries.BAR_MENU_BLOCK.get(),
                BlockRegistries.BAR_COUNTER_BLOCK.get(),
                BlockRegistries.CABINET.get(),
                BlockRegistries.MINT_PLANT.get()
        );
    }

    @Override
    protected void generate() {
        HolderLookup.RegistryLookup<Enchantment> enchantments = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        this.dropSelf(BlockRegistries.BAR_MENU_BLOCK.get());
        this.dropSelf(BlockRegistries.BAR_COUNTER_BLOCK.get());
        this.dropSelf(BlockRegistries.CABINET.get());
        this.add(BlockRegistries.MINT_PLANT.get(), generateMintLoot(enchantments));
    }

    private static final int[][] MINT_DISTRIBUTION = {
            // AGE 2
            {4, 1, 0},
            // AGE 3
            {3, 2, 0},
            // AGE 4
            {2, 3, 1},
            // AGE 5
            {2, 4, 1},
            // AGE 6
            {1, 3, 2},
            // AGE 7
            {1, 2, 2}
    };
    private static LootTable.Builder generateMintLoot(HolderLookup.RegistryLookup<Enchantment> enchantments) {
        LootTable.Builder lootTable = LootTable.lootTable();
        for (int age = 2; age <= MintPlantBlock.MAX_AGE; age++) {
            int[] counts = MINT_DISTRIBUTION[age - 2];
            for (int size = 0; size < 3; size++) {
                LootPool.Builder mintPool = LootPool.lootPool();
                int count = counts[size];
                if (count > 0) {
                    mintPool.add(
                            LootItem.lootTableItem(ItemRegistries.MINT)
                                    .apply(SetComponentsFunction.setComponent(DataComponentTypeRegistries.MINT_SIZE, new MintSizeComponent(size)))
                                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(count)))
                                    .apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))
                                    .when(
                                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(BlockRegistries.MINT_PLANT.get())
                                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(MintPlantBlock.AGE, age))
                                    )
                    );
                }
                lootTable.withPool(mintPool);
            }
        }
        lootTable.withPool(LootPool.lootPool().add(
                LootItem.lootTableItem(ItemRegistries.MINT_SEED)
                        .apply(ApplyBonusCount
                                .addBonusBinomialDistributionCount(
                                        enchantments.getOrThrow(Enchantments.FORTUNE),
                                        0.5714286F,
                                        3)
                        )
                        .when(
                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(BlockRegistries.MINT_PLANT.get())
                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(MintPlantBlock.AGE, MintPlantBlock.MAX_AGE))
                        )
        ));
        lootTable.withPool(LootPool.lootPool().add(
                LootItem.lootTableItem(ItemRegistries.MINT_SEED)
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                        .when(
                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(BlockRegistries.MINT_PLANT.get())
                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(MintPlantBlock.AGE, MintPlantBlock.MAX_AGE))
                        )
        ));

        return lootTable;
    }
}
