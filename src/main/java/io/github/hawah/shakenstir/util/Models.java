package io.github.hawah.shakenstir.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

public enum Models {
    GIN("gin", "block/gin"),
    ;
    private final StandaloneModelKey<QuadCollection> key;
    private final Identifier location;
    Models(String name, String location) {
        this.key = new StandaloneModelKey<>(
                () -> {
                    // A name for the standalone model
                    // Can be any string, but it should contain the mod id
                    return ShakenStir.MODID + ':' + name;
                }
        );
        this.location = Identifier.fromNamespaceAndPath(ShakenStir.MODID, location);
    }

    public StandaloneModelKey<QuadCollection> getKey() {
        return key;
    }

    public void render(PoseStack poseStack, int lightCoords, int overlayCoords) {
        render(poseStack, lightCoords, overlayCoords, RenderTypes.translucentMovingBlock());
    }

    public void render(PoseStack poseStack, int lightCoords, int overlayCoords, RenderType renderType) {
        QuadCollection gin = Minecraft.getInstance().getModelManager().getStandaloneModel(key);
        VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(renderType);
        QuadInstance instance = new QuadInstance();
        instance.setLightCoords(lightCoords);
        instance.setOverlayCoords(overlayCoords);
        gin.getAll().forEach(quad ->
                consumer.putBakedQuad(poseStack.last(), quad, instance)
        );
    }

    public Identifier getLocation() {
        return location;
    }
}
