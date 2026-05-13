package io.github.hawah.shakenstir.client.model;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

public class GlasswareLoaderBuilder extends CustomLoaderBuilder {
    protected GlasswareLoaderBuilder() {
        super(GlasswareUnbakedModelLoader.ID, false);
    }

    @Override
    protected CustomLoaderBuilder copyInternal() {
        ExtendedModelTemplateBuilder.builder()
                .customLoader(GlasswareLoaderBuilder::new, (glasswareLoaderBuilder -> {

                }))
                .parent(ShakenStir.asResource("block/collins_glass"))
                .build();
        return null;
    }
}
