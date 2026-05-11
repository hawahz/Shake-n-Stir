package io.github.hawah.shakenstir.foundation.datagen;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.client.render.item.SpiritBottleSpecialRenderer;
import io.github.hawah.shakenstir.content.HasCup;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.block.SpiritBlock;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.item.SpiritBottleItem;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.item.ConditionalItemModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.renderer.item.properties.numeric.Count;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jspecify.annotations.NonNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModModelProvider extends ModelProvider {

    public ModModelProvider(PackOutput output) {
        super(output, ShakenStir.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateItemWithTintedBaseLayer(ItemRegistries.CONTENT_HOLDER.get(), 0xFFFFFF);
        // Basic single variant model
        registerCustomBlockModel(blockModels, "block/shake_cup", BlockRegistries.SHAKE_CUP_BLOCK.get());
        generateShake(blockModels, itemModels);
        generateSpirit(blockModels, itemModels, BlockRegistries.GIN, ItemRegistries.GIN, "gin");
        generateSpirit(blockModels, itemModels, BlockRegistries.WHISKY, ItemRegistries.WHISKY, "whisky");
        generateEmptyModel(blockModels, BlockRegistries.LONG_DRINK_GLASSWARE.get(), Blocks.GLASS);
        generateEmptyModel(blockModels, BlockRegistries.SHORT_DRINK_GLASSWARE.get(), Blocks.GLASS);

        registerCustomBlockModel(blockModels, "block/whisky_liquid", BlockRegistries.WHISKY_LIQUID.get());
        registerCustomBlockModel(blockModels, "block/vodka_liquid", BlockRegistries.VODKA_LIQUID.get());
        registerCustomBlockModel(blockModels, "block/rum_liquid", BlockRegistries.RUM_LIQUID.get());
        registerCustomBlockModel(blockModels, "block/tequila_liquid", BlockRegistries.TEQUILA_LIQUID.get());
        registerCustomBlockModel(blockModels, "block/brandy_liquid", BlockRegistries.BRANDY_LIQUID.get());
        registerCustomBlockModel(blockModels, "block/gin_liquid", BlockRegistries.GIN_LIQUID.get());
        registerCustomBlockModel(blockModels, "block/whiskey_liquid", BlockRegistries.BUBBLE.get());
        registerCustomBlockModel(blockModels, "block/bubble_liquid", BlockRegistries.BUBBLE_LIQUID.get());

        registerCustomBlockModel(blockModels, "block/vodka", BlockRegistries.VODKA.get());
        registerCustomBlockModel(blockModels, "block/rum", BlockRegistries.RUM.get());
        registerCustomBlockModel(blockModels, "block/tequila", BlockRegistries.TEQUILA.get());
        registerCustomBlockModel(blockModels, "block/brandy", BlockRegistries.BRANDY.get());
        generateIceCube(itemModels);
        generateLongDrinkGlassware(itemModels);
    }

    private static void generateLongDrinkGlassware(ItemModelGenerators itemModels) {
        ItemModel.Unbaked unbaked = ItemModelUtils.plainModel(ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(ItemRegistries.LONG_DRINK_GLASSWARE.get()), TextureMapping.layer0(new Material(ShakenStir.asResource("item/collins_glass"))), itemModels.modelOutput));
        itemModels.itemModelOutput.accept(ItemRegistries.LONG_DRINK_GLASSWARE.get(), unbaked);
    }

    private static void generateIceCube(ItemModelGenerators itemModels) {
        ItemModel.Unbaked iceCube0 = ItemModelUtils.plainModel(itemModels.createFlatItemModel(ItemRegistries.ICE_CUBE.get(), "_0", ModelTemplates.FLAT_ITEM));
        ItemModel.Unbaked iceCube1 = ItemModelUtils.plainModel(itemModels.createFlatItemModel(ItemRegistries.ICE_CUBE.get(), "_1", ModelTemplates.FLAT_ITEM));
        ItemModel.Unbaked iceCube2 = ItemModelUtils.plainModel(itemModels.createFlatItemModel(ItemRegistries.ICE_CUBE.get(), "_2", ModelTemplates.FLAT_ITEM));
        itemModels.itemModelOutput.accept(
                ItemRegistries.ICE_CUBE.get(),
                new RangeSelectItemModel.Unbaked(
                        Optional.empty(),
                        new Count(true),
                        1,
                        List.of(
                                new RangeSelectItemModel.Entry(
                                        0.33F,
                                        iceCube1
                                ),
                                new RangeSelectItemModel.Entry(
                                        0.66F,
                                        iceCube2
                                )
                        ),
                        Optional.of(iceCube0)
                )

        );
    }

    private static void generateShake(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(BlockRegistries.SHAKE_BLOCK.get())
                .with(
                        PropertyDispatch.initial(BlockStateProperties.FACING)
                                .select(Direction.UP, getMultiVariant("block/shake_overall"))
                                .select(Direction.DOWN, getMultiVariant("block/shake_body"))
                                .select(Direction.NORTH, getMultiVariant("block/shake_fall"))
                                .select(Direction.EAST, getMultiVariant("block/shake_fall").with(BlockModelGenerators.Y_ROT_90))
                                .select(Direction.SOUTH, getMultiVariant("block/shake_fall").with(BlockModelGenerators.Y_ROT_180))
                                .select(Direction.WEST, getMultiVariant("block/shake_fall").with(BlockModelGenerators.Y_ROT_270))
                )
        );
        ItemModel.Unbaked openModel = ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(ShakenStir.MODID, "block/shake_body"));
        ItemModel.Unbaked closedModel = ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(ShakenStir.MODID, "block/shake_overall"));
        ConditionalItemModel.Unbaked guiModel = new ConditionalItemModel.Unbaked(
                Optional.empty(),
                new HasCup(),
                closedModel,
                openModel
        );
        itemModels.itemModelOutput.accept(
                ItemRegistries.SHAKE.get(),
                guiModel
        );
    }

    private static void generateSpirit(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Supplier<SpiritBlock> block, DeferredItem<SpiritBottleItem> item, String root) {
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(
                        block.get()
                ).with(
                        PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING, SpiritBlock.COUNTS)
                                .select(Direction.NORTH , 1, getMultiVariant("block/"+root))
                                .select(Direction.EAST  , 1, getMultiVariant("block/"+root).with(BlockModelGenerators.Y_ROT_90))
                                .select(Direction.SOUTH , 1, getMultiVariant("block/"+root).with(BlockModelGenerators.Y_ROT_180))
                                .select(Direction.WEST  , 1, getMultiVariant("block/"+root).with(BlockModelGenerators.Y_ROT_270))
                                .select(Direction.NORTH , 2, getMultiVariant("block/"+root+"_1"))
                                .select(Direction.EAST  , 2, getMultiVariant("block/"+root+"_1").with(BlockModelGenerators.Y_ROT_90))
                                .select(Direction.SOUTH , 2, getMultiVariant("block/"+root+"_1").with(BlockModelGenerators.Y_ROT_180))
                                .select(Direction.WEST  , 2, getMultiVariant("block/"+root+"_1").with(BlockModelGenerators.Y_ROT_270))
                                .select(Direction.NORTH , 3, getMultiVariant("block/"+root+"_2"))
                                .select(Direction.EAST  , 3, getMultiVariant("block/"+root+"_2").with(BlockModelGenerators.Y_ROT_90))
                                .select(Direction.SOUTH , 3, getMultiVariant("block/"+root+"_2").with(BlockModelGenerators.Y_ROT_180))
                                .select(Direction.WEST  , 3, getMultiVariant("block/"+root+"_2").with(BlockModelGenerators.Y_ROT_270))
                                .select(Direction.NORTH , 4, getMultiVariant("block/"+root+"_3"))
                                .select(Direction.EAST  , 4, getMultiVariant("block/"+root+"_3").with(BlockModelGenerators.Y_ROT_90))
                                .select(Direction.SOUTH , 4, getMultiVariant("block/"+root+"_3").with(BlockModelGenerators.Y_ROT_180))
                                .select(Direction.WEST  , 4, getMultiVariant("block/"+root+"_3").with(BlockModelGenerators.Y_ROT_270))
                )
        );
        ItemModel.Unbaked flatModel = ItemModelUtils.plainModel(itemModels.createFlatItemModel(item.get(), ModelTemplates.FLAT_ITEM));
        ItemModel.Unbaked otherModel = ItemModelUtils.specialModel(Identifier.fromNamespaceAndPath(ShakenStir.MODID, "gin"), new SpiritBottleSpecialRenderer.Unbaked());
        itemModels.itemModelOutput.accept(item.get(), ItemModelGenerators.createFlatModelDispatch(flatModel, otherModel));
    }

    private static @NonNull MultiVariant getMultiVariant(String path) {
        return new MultiVariant(
                WeightedList.of(
                        new Variant(Identifier.fromNamespaceAndPath(ShakenStir.MODID, path))
                )
        );
    }

    private static void registerCustomBlockModel(BlockModelGenerators blockModels, String modelPath, Block block) {
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(
                        block,
                        getMultiVariant(modelPath)
                )
        );
    }

    private static void generateEmptyModel(BlockModelGenerators blockModels, Block block) {
        generateEmptyModel(blockModels, block, block);
    }

    private static void generateEmptyModel(BlockModelGenerators blockModels, Block block, Block particle) {
        blockModels.createParticleOnlyBlock(block, particle);
    }

    private static void registerRotatedBlockModel(BlockModelGenerators blockModels, String modelPath, Block block) {
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(
                        block
                ).with(
                        PropertyDispatch.initial(HorizontalDirectionalBlock.FACING)
                                .select(Direction.NORTH, getMultiVariant(modelPath))
                                .select(Direction.EAST, getMultiVariant(modelPath).with(BlockModelGenerators.Y_ROT_90))
                                .select(Direction.SOUTH, getMultiVariant(modelPath).with(BlockModelGenerators.Y_ROT_180))
                                .select(Direction.WEST, getMultiVariant(modelPath).with(BlockModelGenerators.Y_ROT_270))
                )
        );
    }


}