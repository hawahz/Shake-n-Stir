package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.content.LootTableKeys;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.block.MintPlantBlock;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.MintSizeComponent;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.function.BiConsumer;

public class ModBlockInteractLoot implements LootTableSubProvider {

    /**
     * 薄荷收获分布：AGE → {SIZE=0数量, SIZE=1数量, SIZE=2数量}
     */
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
    private final HolderLookup.Provider registries;

    public ModBlockInteractLoot(HolderLookup.Provider lookupProvider) {
        // Store the lookupProvider in a field
        this.registries = lookupProvider;
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        HolderLookup.RegistryLookup<Enchantment> enchantments = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        generateMintLoot(output, enchantments);
    }

    private static void generateMintLoot(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output, HolderLookup.RegistryLookup<Enchantment> enchantments) {
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

        output.accept(
                LootTableKeys.HARVEST_MINT_PLANT,
                lootTable
        );
    }
}
