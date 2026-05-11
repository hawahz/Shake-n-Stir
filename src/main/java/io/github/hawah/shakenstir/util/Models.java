package io.github.hawah.shakenstir.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import java.util.*;

public enum Models implements IModel{
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

    public StandaloneModelKey<QuadCollection> key() {
        return key;
    }
    public static Optional<Mutable> getModel(String key) {
        return Optional.ofNullable(resourcePackModels.get(key));
    }

    public Identifier getLocation() {
        return location;
    }

    public record Mutable(StandaloneModelKey<QuadCollection> key, Identifier location) implements IModel{
        public Mutable(String key) {
            this(new StandaloneModelKey<>(
                    () -> {
                        // A name for the standalone model
                        // Can be any string, but it should contain the mod id
                        return ShakenStir.MODID + ':' + key;
                    }
            ), Identifier.fromNamespaceAndPath(ShakenStir.MODID, "sns/" + key));
        }

        @Override
        public void submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, int lightCoords, int overlayCoords, RenderType renderType, int color) {

        }
    }
} interface IModel {
    StandaloneModelKey<QuadCollection> key();

    default void submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, int lightCoords, int overlayCoords) {
        submit(submitNodeCollector, poseStack, lightCoords, overlayCoords, RenderTypes.translucentMovingBlock());
    };
    default void submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, int lightCoords, int overlayCoords, RenderType renderType) {
        submit(submitNodeCollector, poseStack, lightCoords, overlayCoords, renderType, 0xFFFFFFFF);
    };
    default void submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, int lightCoords, int overlayCoords, RenderType renderType, int color) {
        QuadCollection gin = Minecraft.getInstance().getModelManager().getStandaloneModel(key());
        if (gin == null) {
            return;
        }
        submitNodeCollector.submitCustomGeometry(
                poseStack,
                renderType,
                (pose, buffer) -> {
                    QuadInstance instance = new QuadInstance();
                    instance.setLightCoords(lightCoords);
                    instance.setOverlayCoords(overlayCoords);
                    instance.setColor(color);
                    gin.getAll().forEach(quad ->
                            buffer.putBakedQuad(pose, quad, instance)
                    );
                }
        );
    }
}
