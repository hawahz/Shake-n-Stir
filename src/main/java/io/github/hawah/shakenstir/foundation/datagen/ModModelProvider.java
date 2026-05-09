package io.github.hawah.shakenstir.foundation.datagen;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.client.render.item.SpiritBottleSpecialRenderer;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.NonNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModModelProvider extends ModelProvider {

    public ModModelProvider(PackOutput output) {
        super(output, ShakenStir.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        // Basic single variant model
        registerCustomBlockModel(blockModels, "block/shake_cup", BlockRegistries.SHAKE_CUP_BLOCK.get());
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
        registerRotatedBlockModel(blockModels, "block/gin", BlockRegistries.GIN.get());
        registerCustomBlockModel(blockModels, "block/whisky_liquid", BlockRegistries.WHISKY_LIQUID.get());
        registerCustomBlockModel(blockModels, "block/vodka_liquid", BlockRegistries.VODKA_LIQUID.get());
        registerCustomBlockModel(blockModels, "block/rum_liquid", BlockRegistries.RUM_LIQUID.get());
        registerCustomBlockModel(blockModels, "block/tequila_liquid", BlockRegistries.TEQUILA_LIQUID.get());
        registerCustomBlockModel(blockModels, "block/brandy_liquid", BlockRegistries.BRANDY_LIQUID.get());
        registerCustomBlockModel(blockModels, "block/gin_liquid", BlockRegistries.GIN_LIQUID.get());
        itemModels.generateItemWithTintedBaseLayer(ItemRegistries.CONTENT_HOLDER.get(), 0xFFFFFF);
        ItemModel.Unbaked flatModel = ItemModelUtils.plainModel(itemModels.createFlatItemModel(ItemRegistries.GIN.get(), ModelTemplates.FLAT_ITEM));
        ItemModel.Unbaked otherModel = ItemModelUtils.specialModel(Identifier.fromNamespaceAndPath(ShakenStir.MODID, "gin"), new SpiritBottleSpecialRenderer.Unbaked());
        itemModels.itemModelOutput.accept(ItemRegistries.GIN.get(), ItemModelGenerators.createFlatModelDispatch(flatModel, otherModel));
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