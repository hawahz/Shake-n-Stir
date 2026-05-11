package io.github.hawah.shakenstir.util;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.joml.Vector3f;

import java.util.*;

public enum Models implements IModel{
    MARTINI_GLASS("martini_glass", "block/martini_glass"),
    MARGARITA_GLASS("margarita_glass", "block/margarita_glass"),
    COLLINS_GLASS("collins_glass", "block/collins_glass"),
    ;
    private final StandaloneModelKey<QuadCollection> key;
    private final Identifier location;
    private final ModelData<VoxelShape> voxelShape = new ModelData<>();
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

    public ModelData<VoxelShape> voxelShape() {
        return voxelShape;
    };

    public record Mutable(StandaloneModelKey<QuadCollection> key, Identifier location,ModelData<VoxelShape> voxelShape) implements IModel{

        public Mutable(String key) {
            this(new StandaloneModelKey<>(
                    () -> {
                        // A name for the standalone model
                        // Can be any string, but it should contain the mod id
                        return ShakenStir.MODID + ':' + key;
                    }
            ), Identifier.fromNamespaceAndPath(ShakenStir.MODID, "sns/" + key), new ModelData<>());
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
