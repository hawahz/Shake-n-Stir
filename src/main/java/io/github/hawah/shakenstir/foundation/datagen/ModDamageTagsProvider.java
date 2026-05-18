package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.foundation.tags.SnsDamageTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class ModDamageTagsProvider extends DamageTypeTagsProvider {
    public ModDamageTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, ShakenStir.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(SnsDamageTags.PARALYSIS_DEADLY_PREVENTION)
                .addTag(Tags.DamageTypes.IS_PHYSICAL)
                .addTag(Tags.DamageTypes.IS_ENVIRONMENT)
                ;
    }
}
