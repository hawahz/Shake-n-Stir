package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import io.github.hawah.shakenstir.foundation.tags.SnsFluidTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class ModFluidTagsProvider extends FluidTagsProvider {
    public ModFluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, ShakenStir.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(SnsFluidTags.SPIRIT)
                .add(FluidRegistries.GIN_SOURCE_FLUID_BLOCK.get())
                .add(FluidRegistries.GIN_FLOWING_FLUID_BLOCK.get())
                .add(FluidRegistries.VODKA_SOURCE_FLUID_BLOCK.get())
                .add(FluidRegistries.VODKA_FLOWING_FLUID_BLOCK.get())
                .add(FluidRegistries.WHISKY_SOURCE_FLUID_BLOCK.get())
                .add(FluidRegistries.WHISKY_FLOWING_FLUID_BLOCK.get())
                .add(FluidRegistries.BRANDY_SOURCE_FLUID_BLOCK.get())
                .add(FluidRegistries.BRANDY_FLOWING_FLUID_BLOCK.get())
                .add(FluidRegistries.RUM_SOURCE_FLUID_BLOCK.get())
                .add(FluidRegistries.RUM_FLOWING_FLUID_BLOCK.get())
                .add(FluidRegistries.TEQUILA_SOURCE_FLUID_BLOCK.get())
                .add(FluidRegistries.TEQUILA_FLOWING_FLUID_BLOCK.get());

        this.tag(Tags.Fluids.HIDDEN_FROM_RECIPE_VIEWERS).addTag(SnsFluidTags.SPIRIT);

        this.tag(SnsFluidTags.BUBBLE_LIKE)
                .add(FluidRegistries.BUBBLE_SOURCE_FLUID_BLOCK.get());
    }
}
