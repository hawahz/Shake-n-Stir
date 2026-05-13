package io.github.hawah.shakenstir.util;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.client.model.GlasswareQuadCollection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import java.util.*;

public enum Models implements IModel<QuadCollection>{
    MARTINI_GLASS("martini_glass", "block/martini_glass"),
    MARGARITA_GLASS("margarita_glass", "block/margarita_glass"),
    COLLINS_GLASS("collins_glass", "block/collins_glass"),
    ;
    private final StandaloneModelKey<QuadCollection> key;
    private final Identifier location;
    private final ModelData<VoxelShape> voxelShape = new ModelData<>();
    Models(String name, String location) {
        this.key = new StandaloneModelKey<>(
                () -> ShakenStir.MODID + ':' + name
        );
        this.location = Identifier.fromNamespaceAndPath(ShakenStir.MODID, location);
    }

    public static final Map<Identifier, Glassware> glasswareModels = new HashMap<>();
    public static final Map<Identifier, Mutable> resourcePackModels = new HashMap<>();

    public static void registerModel(Identifier path) {
        resourcePackModels.put(path, new Mutable(ShakenStir.asResource("glassware/" + path.getPath())));
    }

    public static void registerGlassware(Identifier path) {
        glasswareModels.put(path, new Glassware(ShakenStir.asResource("glassware/" + path.getPath())));
    }

    public static void buildModelsFromResourcePack() {
        for (Models model : Models.values()) {
            model.voxelShape.value = null;
        }
        glasswareModels.clear();
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        FileToIdConverter MODEL_LISTER = FileToIdConverter.json("models/glassware");
        Map<Identifier, Resource> resources = MODEL_LISTER.listMatchingResources(resourceManager);
        Models.glasswareModels.clear();
        resources.forEach(
                (id, _) -> {
                    if (!id.getNamespace().equals(ShakenStir.MODID)) {
                        return;
                    }
                    String key = id.getPath().substring(MODEL_LISTER.prefix().length() + 1, id.getPath().length() - MODEL_LISTER.extension().length());
                    Models.registerGlassware(ShakenStir.asResource(key));
                }
        );
    }

    public StandaloneModelKey<QuadCollection> key() {
        return key;
    }
    public static Optional<IModel<?>> getModel(Identifier key) {
        return glasswareModels.containsKey(key)?
                Optional.of(Models.glasswareModels.get(key)):
                resourcePackModels.containsKey(key)?
                        Optional.of(Models.resourcePackModels.get(key)):
                        Optional.empty();
    }

    public Identifier getLocation() {
        return location;
    }

    public ModelData<VoxelShape> voxelShape() {
        return voxelShape;
    };

    public record Mutable(StandaloneModelKey<QuadCollection> key, Identifier location,ModelData<VoxelShape> voxelShape) implements IModel<QuadCollection>{

        public Mutable(Identifier key) {
            // A name for the standalone model
            // Can be any string, but it should contain the mod id
            this(new StandaloneModelKey<>(
                    key::toString
            ), key, new ModelData<>());
        }

        @Override
        public ModelData<VoxelShape> voxelShape() {
            return voxelShape;
        }
    }

    public record Glassware(StandaloneModelKey<GlasswareQuadCollection> key, Identifier location, ModelData<VoxelShape> voxelShape) implements IModel<GlasswareQuadCollection>{

        public Glassware(Identifier key) {
            // A name for the standalone model
            // Can be any string, but it should contain the mod id
            this(new StandaloneModelKey<>(
                    key::toString
            ), key, new ModelData<>());
        }

        @Override
        public ModelData<VoxelShape> voxelShape() {
            return voxelShape;
        }
    }

    public static class ModelData<T> {

        private T value = null;

        public ModelData() {
        }

        public T get() {
            return value;
        }

        boolean mutable() {
            return value == null;
        }

        void setValue(T value) {
            if (!mutable()) {
                return;
            }
            this.value = value;
        }
    }
}
