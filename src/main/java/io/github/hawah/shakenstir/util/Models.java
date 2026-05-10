package io.github.hawah.shakenstir.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import java.awt.*;
import java.util.*;
import java.util.List;

public enum Models {
    GIN("gin", "block/gin"),
    MARTINI_GLASS("martini_glass", "block/martini_glass"),
    MARGARITA_GLASS("margarita_glass", "block/margarita_glass"),
    COLLINS_GLASS("collins_glass", "block/collins_glass"),
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

    public static Map<String, Mutable> resourcePackModels = new HashMap<>();

    public static void registerModel(String path) {
        resourcePackModels.put(path, new Mutable(path));
    }

    public static void buildModelsFromResourcePack() {
        resourcePackModels.clear();
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        FileToIdConverter MODEL_LISTER = FileToIdConverter.json("models/sns");
        Map<Identifier, Resource> resources = MODEL_LISTER.listMatchingResources(resourceManager);
        Models.resourcePackModels.clear();
        resources.forEach(
                (id, _) -> {
                    if (!id.getNamespace().equals(ShakenStir.MODID)) {
                        return;
                    }
                    String key = id.getPath().substring(MODEL_LISTER.prefix().length() + 1, id.getPath().length() - MODEL_LISTER.extension().length());
                    Models.registerModel(key);
                }
        );
    }

    public StandaloneModelKey<QuadCollection> getKey() {
        return key;
    }

    public void render(PoseStack poseStack, int lightCoords, int overlayCoords) {
        render(poseStack, lightCoords, overlayCoords, RenderTypes.translucentMovingBlock());
    }

    public static Optional<Mutable> getModel(String key) {
        return Optional.ofNullable(resourcePackModels.get(key));
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

    public void render(PoseStack poseStack, int lightCoords, int overlayCoords, RenderType renderType, int color) {
        QuadCollection gin = Minecraft.getInstance().getModelManager().getStandaloneModel(key);
        VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(renderType);
        QuadInstance instance = new QuadInstance();
        instance.setLightCoords(lightCoords);
        instance.setOverlayCoords(overlayCoords);
        instance.setColor(color);
        gin.getAll().forEach(quad ->
                consumer.putBakedQuad(poseStack.last(), quad, instance)
        );
    }

    public Identifier getLocation() {
        return location;
    }

    public record Mutable(StandaloneModelKey<QuadCollection> key, Identifier location) {
        public Mutable(String key) {
            this(new StandaloneModelKey<>(
                    () -> {
                        // A name for the standalone model
                        // Can be any string, but it should contain the mod id
                        return ShakenStir.MODID + ':' + key;
                    }
            ), Identifier.fromNamespaceAndPath(ShakenStir.MODID, "sns/" + key));
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

        public void render(PoseStack poseStack, int lightCoords, int overlayCoords, RenderType renderType, int color) {
            QuadCollection gin = Minecraft.getInstance().getModelManager().getStandaloneModel(key);
            VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(renderType);
            QuadInstance instance = new QuadInstance();
            instance.setLightCoords(lightCoords);
            instance.setOverlayCoords(overlayCoords);
            instance.setColor(color);
            gin.getAll().forEach(quad ->
                    consumer.putBakedQuad(poseStack.last(), quad, instance)
            );
        }
    }
}
