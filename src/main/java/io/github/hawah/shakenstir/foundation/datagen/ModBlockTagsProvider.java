package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.foundation.tags.SnsBlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, ShakenStir.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(SnsBlockTags.BLOCKING_FLUID)
                .add(BlockRegistries.GIN.get())
                .add(BlockRegistries.VODKA.get())
                .add(BlockRegistries.WHISKY.get())
                .add(BlockRegistries.RUM.get())
                .add(BlockRegistries.TEQUILA.get())
                .add(BlockRegistries.BRANDY.get())
                .add(BlockRegistries.BUBBLE.get())
                .add(BlockRegistries.SHAKE_BLOCK.get())
                .add(BlockRegistries.SHAKE_LID_BLOCK.get())
                .add(BlockRegistries.CABINET.get())
                .add(BlockRegistries.BOTTLE.get())
                .add(BlockRegistries.DISTILLER.get())
        ;
        this.tag(SnsBlockTags.BAR_AREA_IGNORED)
                .addTag(BlockTags.AIR)
                .addTag(BlockTags.SIGNS)
                .add(BlockRegistries.BAR_MENU_BLOCK.get())
                .addTag(BlockTags.CANDLES)
        ;

        this.tag(BlockTags.MINEABLE_WITH_AXE)
                .add(BlockRegistries.BAR_COUNTER_BLOCK.get())
                .add(BlockRegistries.CABINET.get())
        ;

        this.tag(BlockTags.LOGS)
                .add(BlockRegistries.LEMON_LOG.get())
        ;

        this.tag(BlockTags.LEAVES)
                .add(BlockRegistries.LEMON_SIDE_LEAVES.get())
        ;
    }
}
