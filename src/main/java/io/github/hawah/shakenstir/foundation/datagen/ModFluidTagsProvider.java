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

@SuppressWarnings("unchecked")
@ParametersAreNonnullByDefault
public class ModFluidTagsProvider extends FluidTagsProvider {
    public ModFluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, ShakenStir.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(SnsFluidTags.GIN)
                .add(FluidRegistries.GIN_SOURCE.get());

        this.tag(SnsFluidTags.VODKA)
                .add(FluidRegistries.VODKA_SOURCE.get());

        this.tag(SnsFluidTags.WHISKY)
                .add(FluidRegistries.WHISKY_SOURCE.get());

        this.tag(SnsFluidTags.BRANDY)
                .add(FluidRegistries.BRANDY_SOURCE.get());

        this.tag(SnsFluidTags.RUM)
                .add(FluidRegistries.RUM_SOURCE.get());

        this.tag(SnsFluidTags.TEQUILA)
                .add(FluidRegistries.TEQUILA_SOURCE.get());

        this.tag(SnsFluidTags.SPIRIT)
                .addTags(SnsFluidTags.GIN, SnsFluidTags.VODKA, SnsFluidTags.WHISKY, SnsFluidTags.BRANDY, SnsFluidTags.RUM, SnsFluidTags.TEQUILA);

        this.tag(Tags.Fluids.HIDDEN_FROM_RECIPE_VIEWERS).addTag(SnsFluidTags.SPIRIT);

        this.tag(SnsFluidTags.BUBBLE_LIKE)
                .add(FluidRegistries.BUBBLE_SOURCE.get())
                .add(FluidRegistries.TONIC_SOURCE.get())
        ;

        this.tag(Tags.Fluids.HONEY)
                .add(FluidRegistries.HONEY_SOURCE.get())
        ;

        this.tag(SnsFluidTags.SWEET)
                .addTag(Tags.Fluids.HONEY)
                .add(FluidRegistries.SWEET_JUICE_SOURCE.get())
                .add(FluidRegistries.SUCROSE_SYRUP_SOURCE.get())
        ;

        this.tag(SnsFluidTags.LEMONADE)
                .add(FluidRegistries.LEMONADE_SOURCE.get())
        ;

        this.tag(SnsFluidTags.SOUR)
                .addTag(SnsFluidTags.LEMONADE)
        ;
        this.tag(SnsFluidTags.BITTER)
                .add(FluidRegistries.BITTERS_SOURCE.get())
        ;
        this.tag(SnsFluidTags.JUICE)
                .add(FluidRegistries.JUICE_SOURCE.get())
                .add(FluidRegistries.SWEET_JUICE_SOURCE.get())
                .addTag(SnsFluidTags.LEMONADE)
        ;
    }
}
