package io.github.hawah.shakenstir.foundation.datagen;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.client.render.item.GlasswareSpecialRenderer;
import io.github.hawah.shakenstir.client.render.item.ShakeItemSpecialRenderer;
import io.github.hawah.shakenstir.client.render.item.SpiritBottleSpecialRenderer;
import io.github.hawah.shakenstir.content.HasCup;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.block.Cabinet;
import io.github.hawah.shakenstir.content.block.Distiller;
import io.github.hawah.shakenstir.foundation.block.DistillerPart;
import io.github.hawah.shakenstir.content.block.SpiritBlock;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.item.SpiritBottleItem;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ConditionalItemModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.renderer.item.properties.numeric.Count;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.registries.DeferredItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@SuppressWarnings({"DuplicatedCode", "ExtractMethodRecommender"})
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModModelProvider extends ModelProvider {

    public ModModelProvider(PackOutput output) {
        super(output, ShakenStir.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateItemWithTintedBaseLayer(ItemRegistries.CONTENT_HOLDER.get(), 0xFFFFFF);
        itemModels.generateFlatItem(ItemRegistries.LEMON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ItemRegistries.LEMON_SLICE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ItemRegistries.SOBERING_TEA.get(), ModelTemplates.FLAT_ITEM);
        // Basic single variant model
        registerCustomBlockModel(blockModels, "block/shake_cup", BlockRegistries.SHAKE_CUP_BLOCK.get());
        generateShake(blockModels, itemModels);
        generateSpirit(blockModels, itemModels, BlockRegistries.GIN, ItemRegistries.GIN, "gin");
        generateSpirit(blockModels, itemModels, BlockRegistries.WHISKY, ItemRegistries.WHISKY, "whisky");
        generateSpirit(blockModels, itemModels, BlockRegistries.VODKA, ItemRegistries.VODKA, "vodka");
        generateSpirit(blockModels, itemModels, BlockRegistries.RUM, ItemRegistries.RUM, "rum");
        generateSpirit(blockModels, itemModels, BlockRegistries.TEQUILA, ItemRegistries.TEQUILA, "tequila");
        generateSpirit(blockModels, itemModels, BlockRegistries.BRANDY, ItemRegistries.BRANDY, "brandy");
        generateSpirit(blockModels, itemModels, BlockRegistries.BOTTLE, ItemRegistries.BOTTLE, "bottle");
        generateEmptyModel(blockModels, BlockRegistries.LONG_DRINK_GLASSWARE.get(), Blocks.GLASS);
        generateEmptyModel(blockModels, BlockRegistries.SHORT_DRINK_GLASSWARE.get(), Blocks.GLASS);
        initGlassware(itemModels);
        generateGlassware(ShakenStir.asResource("martini_glass"), itemModels);
        generateGlassware(ShakenStir.asResource("collins_glass"), itemModels);
        generateGlassware(ShakenStir.asResource("margarita_glass"), itemModels);

        registerCustomBlockModel(blockModels, "block/whisky_liquid", BlockRegistries.WHISKY_LIQUID.get());
        registerCustomBlockModel(blockModels, "block/vodka_liquid", BlockRegistries.VODKA_LIQUID.get());
        registerCustomBlockModel(blockModels, "block/rum_liquid", BlockRegistries.RUM_LIQUID.get());
        registerCustomBlockModel(blockModels, "block/tequila_liquid", BlockRegistries.TEQUILA_LIQUID.get());
        registerCustomBlockModel(blockModels, "block/brandy_liquid", BlockRegistries.BRANDY_LIQUID.get());
        registerCustomBlockModel(blockModels, "block/gin_liquid", BlockRegistries.GIN_LIQUID.get());
        registerCustomBlockModel(blockModels, "block/whiskey_liquid", BlockRegistries.BUBBLE.get());
        registerCustomBlockModel(blockModels, "block/bubble_liquid", BlockRegistries.BUBBLE_LIQUID.get());

        generateIceCube(itemModels);
        generateCabinet(blockModels, itemModels);
        generateDistiller(blockModels, itemModels);
//        generateLongDrinkGlassware(itemModels);
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

        ItemModel.Unbaked thirdPersonModel = ItemModelUtils.specialModel(Identifier.fromNamespaceAndPath(ShakenStir.MODID, "gin"), new ShakeItemSpecialRenderer.Unbaked());

        ItemModel.Unbaked selected = ItemModelUtils.select(
                new DisplayContext(),
                guiModel,
                ItemModelUtils.when(List.of(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND), thirdPersonModel)
        );

        itemModels.itemModelOutput.accept(
                ItemRegistries.SHAKE.get(),
                selected
        );
    }
    private static void generateCabinet(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(BlockRegistries.CABINET.get())
                .with(
                        PropertyDispatch.initial(Cabinet.LEFT, Cabinet.RIGHT, Cabinet.FACING)
                                .select(false,  false,  Direction.NORTH, getMultiVariant("block/cabinet"))
                                .select(false,   true,  Direction.NORTH, getMultiVariant("block/cabinet_l"))
                                .select(true,  false,   Direction.NORTH, getMultiVariant("block/cabinet_r"))
                                .select(true,   true,   Direction.NORTH, getMultiVariant("block/cabinet_lr"))
                                .select(false,  false,  Direction.EAST, getMultiVariant("block/cabinet")    .with(BlockModelGenerators.Y_ROT_90))
                                .select(false,   true,  Direction.EAST, getMultiVariant("block/cabinet_l")  .with(BlockModelGenerators.Y_ROT_90))
                                .select(true,  false,   Direction.EAST, getMultiVariant("block/cabinet_r")   .with(BlockModelGenerators.Y_ROT_90))
                                .select(true,   true,   Direction.EAST, getMultiVariant("block/cabinet_lr") .with(BlockModelGenerators.Y_ROT_90))
                                .select(false,  false,  Direction.SOUTH, getMultiVariant("block/cabinet")   .with(BlockModelGenerators.Y_ROT_180))
                                .select(false,   true,  Direction.SOUTH, getMultiVariant("block/cabinet_l") .with(BlockModelGenerators.Y_ROT_180))
                                .select(true,  false,   Direction.SOUTH, getMultiVariant("block/cabinet_r")  .with(BlockModelGenerators.Y_ROT_180))
                                .select(true,   true,   Direction.SOUTH, getMultiVariant("block/cabinet_lr").with(BlockModelGenerators.Y_ROT_180))
                                .select(false,  false,  Direction.WEST, getMultiVariant("block/cabinet")   .with(BlockModelGenerators.Y_ROT_270))
                                .select(false,   true,  Direction.WEST, getMultiVariant("block/cabinet_l") .with(BlockModelGenerators.Y_ROT_270))
                                .select(true,  false,   Direction.WEST, getMultiVariant("block/cabinet_r")  .with(BlockModelGenerators.Y_ROT_270))
                                .select(true,   true,   Direction.WEST, getMultiVariant("block/cabinet_lr").with(BlockModelGenerators.Y_ROT_270))
                )
        );
        ItemModel.Unbaked itemModel = ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(ShakenStir.MODID, "block/cabinet"));

        itemModels.itemModelOutput.accept(
                ItemRegistries.CABINET.get(),
                itemModel
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

    private static MultiVariant getMultiVariant(String path) {
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

    public void generateDistiller(BlockModelGenerators blockModels, ItemModelGenerators itemModelGenerators) {
        MultiVariant bottom = BlockModelGenerators.plainVariant(ShakenStir.asResource("block/distillation_barrel_bottom"));
        MultiVariant top = BlockModelGenerators.plainVariant(ShakenStir.asResource("block/distillation_barrel_top"));
        MultiVariant pipe = BlockModelGenerators.plainVariant(ShakenStir.asResource("block/distillation_pipe"));
        Block barrel = BlockRegistries.DISTILLER.get();
        blockModels.blockStateOutput
                .accept(
                        generateDistiller(barrel, bottom, top, pipe)
                );

        ItemModel.Unbaked itemModel = ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(ShakenStir.MODID, "item/distiller_item"));

        itemModelGenerators.itemModelOutput.accept(
                ItemRegistries.DISTILLER.get(),
                itemModel
        );
    }

    public static BlockModelDefinitionGenerator generateDistiller(
            Block block,
            MultiVariant bottom,
            MultiVariant top,
            MultiVariant pipe
    ) {
        return MultiVariantGenerator.dispatch(block)
                .with(
                        PropertyDispatch.initial(Distiller.FACING, Distiller.PART)
                                .select(Direction.NORTH, DistillerPart.LOWER, bottom)
                                .select(Direction.EAST, DistillerPart.LOWER, bottom.with(BlockModelGenerators.Y_ROT_90))
                                .select(Direction.SOUTH, DistillerPart.LOWER, bottom.with(BlockModelGenerators.Y_ROT_180))
                                .select(Direction.WEST, DistillerPart.LOWER, bottom.with(BlockModelGenerators.Y_ROT_270))
                                .select(Direction.NORTH, DistillerPart.UPPER, top)
                                .select(Direction.EAST, DistillerPart.UPPER, top.with(BlockModelGenerators.Y_ROT_90))
                                .select(Direction.SOUTH, DistillerPart.UPPER, top.with(BlockModelGenerators.Y_ROT_180))
                                .select(Direction.WEST, DistillerPart.UPPER, top.with(BlockModelGenerators.Y_ROT_270))
                                .select(Direction.NORTH, DistillerPart.PIPE, pipe)
                                .select(Direction.EAST, DistillerPart.PIPE, pipe.with(BlockModelGenerators.Y_ROT_90))
                                .select(Direction.SOUTH, DistillerPart.PIPE, pipe.with(BlockModelGenerators.Y_ROT_180))
                                .select(Direction.WEST, DistillerPart.PIPE, pipe.with(BlockModelGenerators.Y_ROT_270))
                );
    }

    private static void generateEmptyModel(BlockModelGenerators blockModels, Block block, Block particle) {
        blockModels.createParticleOnlyBlock(block, particle);
    }

    public void generateTwoLayerDyedItem(Item item, ItemModelGenerators itemModels) {
        Material baseLayer = TextureMapping.getItemTexture(item);
        Material tintedLayer = TextureMapping.getItemTexture(item, "_overlay");
        Identifier plainModel = ModelTemplates.FLAT_ITEM.create(item, TextureMapping.layer0(baseLayer), itemModels.modelOutput);
        Identifier dyedModel = ModelLocationUtils.getModelLocation(item, "_dyed");
        ModelTemplates.TWO_LAYERED_ITEM.create(dyedModel, TextureMapping.layered(baseLayer, tintedLayer), itemModels.modelOutput);
        itemModels.itemModelOutput
                .accept(
                        item,
                        ItemModelUtils.conditional(
                                ItemModelUtils.hasComponent(DataComponents.DYED_COLOR),
                                ItemModelUtils.tintedModel(dyedModel, ItemModelGenerators.BLANK_LAYER, new Dye(0)),
                                ItemModelUtils.plainModel(plainModel)
                        )
                );
    }

//    public void generateGlassware(Identifier base, ItemModelGenerators itemModels) {
//        Material baseLayer = new Material(base.withPrefix("item/")); // texture/base
//        Material tintedLayer = new Material(base.withPrefix("item/").withSuffix("_overlay")); // texture/base_overlay
//        Identifier plainModel = ModelTemplates.FLAT_ITEM.create(base.withPrefix("item/"), TextureMapping.layer0(baseLayer), itemModels.modelOutput); //  texture
//        Identifier dyedModel = base.withPrefix("item/").withSuffix("_dyed"); //  model/base_dyed
//        ModelTemplates.TWO_LAYERED_ITEM.create(dyedModel, TextureMapping.layered(baseLayer, tintedLayer), itemModels.modelOutput);
//        ItemModel.Unbaked guiModel = ItemModelUtils.conditional(
//                ItemModelUtils.hasComponent(DataComponents.DYED_COLOR),
//                ItemModelUtils.tintedModel(dyedModel, ItemModelGenerators.BLANK_LAYER, new Dye(0)),
//                ItemModelUtils.plainModel(plainModel)
//        );
//
//        ItemModel.Unbaked otherModel = ItemModelUtils.specialModel(Identifier.fromNamespaceAndPath(ShakenStir.MODID, "glassware_special"), new GlasswareSpecialRenderer.Unbaked());
//
//        itemModels.itemModelOutput
//                .register(
//                        base,
//                        new ClientItem(ItemModelGenerators.createFlatModelDispatch(guiModel, otherModel), ClientItem.Properties.DEFAULT)
//                );
//    }

    Material flowerLayer;
    Identifier flowerModelLoc;
    Material lemonLayer;
    Identifier lemonModelLoc;

    public void initGlassware(ItemModelGenerators itemModels) {
        flowerLayer      = new Material(ShakenStir.asResource("item/flower_overlay"));
        flowerModelLoc   = ModelTemplates.FLAT_ITEM.create(ShakenStir.asResource("item/flower_overlay"), TextureMapping.layer0(flowerLayer), itemModels.modelOutput);
        lemonLayer       = new Material(ShakenStir.asResource("item/lemon_overlay"));
        lemonModelLoc    = ModelTemplates.FLAT_ITEM.create(ShakenStir.asResource("item/lemon_overlay"), TextureMapping.layer0(lemonLayer), itemModels.modelOutput);
    }

    public void generateGlassware(Identifier base, ItemModelGenerators itemModels) {
        Material baseLayer = new Material(base.withPrefix("item/")); // texture/base
        Material tintedLayer = new Material(base.withPrefix("item/").withSuffix("_overlay")); // texture/base_overlay
        Identifier plainModel = ModelTemplates.FLAT_ITEM.create(base.withPrefix("item/"), TextureMapping.layer0(baseLayer), itemModels.modelOutput); //  texture
        Identifier dyedModel = base.withPrefix("item/").withSuffix("_dyed"); //  model/base_dyed
        ModelTemplates.TWO_LAYERED_ITEM.create(dyedModel, TextureMapping.layered(baseLayer, tintedLayer), itemModels.modelOutput);
        ItemModel.Unbaked guiModelBase = ItemModelUtils.conditional(
                ItemModelUtils.hasComponent(DataComponents.DYED_COLOR),
                ItemModelUtils.tintedModel(dyedModel, ItemModelGenerators.BLANK_LAYER, new Dye(0)),
                ItemModelUtils.plainModel(plainModel)
        );



        ItemModel.Unbaked flowerModel = ItemModelUtils.plainModel(flowerModelLoc);
        ItemModel.Unbaked lemonModel = ItemModelUtils.plainModel(lemonModelLoc);

        ItemModel.Unbaked baseWithFlower = ItemModelUtils.composite(guiModelBase, flowerModel);
        ItemModel.Unbaked baseWithLemon = ItemModelUtils.composite(guiModelBase, lemonModel);
        ItemModel.Unbaked baseWithFlowerAndLemon = ItemModelUtils.composite(flowerModel, baseWithLemon);

        ItemModel.Unbaked guiModel = ItemModelUtils.conditional(
                ItemModelUtils.hasComponent(DataComponentTypeRegistries.GLASSWARE_HAS_FLOWER),
                ItemModelUtils.conditional(
                        ItemModelUtils.hasComponent(DataComponentTypeRegistries.GLASSWARE_HAS_LEMON),
                        baseWithFlowerAndLemon,
                        baseWithFlower
                ),
                ItemModelUtils.conditional(
                        ItemModelUtils.hasComponent(DataComponentTypeRegistries.GLASSWARE_HAS_LEMON),
                        baseWithLemon,
                        guiModelBase
                )
        );



        ItemModel.Unbaked otherModel = ItemModelUtils.specialModel(Identifier.fromNamespaceAndPath(ShakenStir.MODID, "glassware_special"), new GlasswareSpecialRenderer.Unbaked());

        itemModels.itemModelOutput
                .register(
                        base,
                        new ClientItem(ItemModelGenerators.createFlatModelDispatch(guiModel, otherModel), ClientItem.Properties.DEFAULT)
                );
    }

    public void generateTwoLayerDyedItem(Identifier base, ItemModelGenerators itemModels) {
        Material baseLayer = new Material(base.withPrefix("item/")); // texture/base
        Material tintedLayer = new Material(base.withPrefix("item/").withSuffix("_overlay")); // texture/base_overlay
        Identifier plainModel = ModelTemplates.FLAT_ITEM.create(base.withPrefix("item/"), TextureMapping.layer0(baseLayer), itemModels.modelOutput); //  texture
        Identifier dyedModel = base.withPrefix("item/").withSuffix("_dyed"); //  model/base_dyed
        ModelTemplates.TWO_LAYERED_ITEM.create(dyedModel, TextureMapping.layered(baseLayer, tintedLayer), itemModels.modelOutput);
        ItemModel.Unbaked conditionalModel = ItemModelUtils.conditional(
                ItemModelUtils.hasComponent(DataComponents.DYED_COLOR),
                ItemModelUtils.tintedModel(dyedModel, ItemModelGenerators.BLANK_LAYER, new Dye(0)),
                ItemModelUtils.plainModel(plainModel)
        );

        itemModels.itemModelOutput
                .register(
                        base,
                        new ClientItem(conditionalModel, ClientItem.Properties.DEFAULT)
                );
    }

    private static void generateUnregisteredItemModel(String modelNTexturePath, ItemModelGenerators itemModels) {
        Identifier identifier = ModelTemplates.FLAT_ITEM.create(
                ShakenStir.asResource("item/" + modelNTexturePath),
                new TextureMapping().put(TextureSlot.LAYER0, new Material(ShakenStir.asResource("item/" + modelNTexturePath))),
                itemModels.modelOutput
        );
        ItemModel.Unbaked model = ItemModelUtils.plainModel(identifier);
        itemModels.itemModelOutput.register(
                ShakenStir.asResource(modelNTexturePath), new ClientItem(model, ClientItem.Properties.DEFAULT)
        );
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