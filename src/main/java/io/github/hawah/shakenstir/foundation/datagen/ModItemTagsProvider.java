package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.tags.SnsItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unchecked")
public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, ShakenStir.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(SnsItemTags.SPIRIT)
                .add(
                        ItemRegistries.GIN.get(),
                        ItemRegistries.WHISKY.get()
                );
        this.tag(SnsItemTags.SOUR)
                .add(Items.ROTTEN_FLESH)
                .addTag(Tags.Items.FOODS_FRUIT);

        this.tag(SnsItemTags.SWEET)
                .addTag(Tags.Items.DRINKS_HONEY)
                .addTag(Tags.Items.FOODS_BERRY)
                //.addTag(Tags.Items.FOODS_CANDY)
                .add(Items.HONEYCOMB)
                .add(Items.SUGAR);

        this.tag(SnsItemTags.BITTER)
                .add(Items.SPIDER_EYE)
                .addTag(Tags.Items.DRINKS_OMINOUS)
                ;
        this.tag(SnsItemTags.SHAKE_PLACABLE)
                .addTags(SnsItemTags.SOUR, SnsItemTags.SWEET, SnsItemTags.BITTER)
                .add(ItemRegistries.ICE_CUBE.get())
                ;
        this.tag(SnsItemTags.BUBBLE_LIKE);
        this.tag(SnsItemTags.GLASSWARE);
        this.tag(SnsItemTags.BLOCK_LIKE_DRINK_DECORATION)
                .addTag(Tags.Items.FLOWERS_SMALL)

                ;
        this.tag(SnsItemTags.ITEM_LIKE_DRINK_DECORATION)
                .add(Items.MELON_SLICE)
                .add(Items.SWEET_BERRIES)
                .add(Items.GLOW_BERRIES)
                .add(Items.BAMBOO)
                .add(ItemRegistries.LEMON_SLICE.get())
                .add();
        this.tag(SnsItemTags.DRINK_DECORATION)
                .addTags(SnsItemTags.BLOCK_LIKE_DRINK_DECORATION, SnsItemTags.ITEM_LIKE_DRINK_DECORATION);
    }
}
