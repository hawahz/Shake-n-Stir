package io.github.hawah.shakenstir.client.model.glassware;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import org.joml.Vector3d;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GlasswareUnbakedModelLoader implements UnbakedModelLoader<GlasswareUnbakedModel>, ResourceManagerReloadListener {

    public static final GlasswareUnbakedModelLoader INSTANCE = new GlasswareUnbakedModelLoader();
    public static final Identifier ID = ShakenStir.asResource("glassware_loader");

    private GlasswareUnbakedModelLoader() {}

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {

    }

    @Override
    public GlasswareUnbakedModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        JsonObject fluidShape = jsonObject.getAsJsonObject("fluidShape");
        double minX, maxX, minY, maxY, minZ, maxZ;
        if (fluidShape.has("minX")) {
            minX = fluidShape.get("minX").getAsDouble();
            minY = fluidShape.get("minY").getAsDouble();
            minZ = fluidShape.get("minZ").getAsDouble();
            maxX = fluidShape.get("maxX").getAsDouble();
            maxY = fluidShape.get("maxY").getAsDouble();
            maxZ = fluidShape.get("maxZ").getAsDouble();
        } else if (fluidShape.has("posX")) {
            minX = fluidShape.get("posX").getAsDouble();
            minY = fluidShape.get("posY").getAsDouble();
            minZ = fluidShape.get("posZ").getAsDouble();
            maxX = minX + fluidShape.get("sizeX").getAsDouble();
            maxY = minY + fluidShape.get("sizeY").getAsDouble();
            maxZ = minZ + fluidShape.get("sizeZ").getAsDouble();
        } else {
            minX = 0;
            minY = 0;
            minZ = 0;
            maxX = 1;
            maxY = 1;
            maxZ = 1;
        }
        StandardModelParameters params = StandardModelParameters.parse(jsonObject, deserializationContext);
        Either<Identifier, UnbakedModel> parent = jsonObject.has("parent") ?
                Either.left(Identifier.parse(jsonObject.get("parent").getAsString())) :
                Either.right(deserializationContext.deserialize(jsonObject, CuboidModel.class));
        //jsonObject.remove("fluidShape");
        GlasswareUnbakedGeometry geometry = new GlasswareUnbakedGeometry(parent);
        GlasswareRenderParameters glasswareRenderParameters = new GlasswareRenderParameters(
                new Vector3d(minX, minY, minZ),
                new Vector3d(maxX, maxY, maxZ)
        );
        return new GlasswareUnbakedModel(params, geometry, glasswareRenderParameters);
    }

    private static void readChildren(
            JsonObject jsonObject,
            String name,
            ImmutableMap.Builder<String, Either<Identifier, UnbakedModel>> children,
            JsonDeserializationContext context) {
        if (!jsonObject.has(name))
            return;
        var childrenJsonObject = jsonObject.getAsJsonObject(name);
        for (Map.Entry<String, JsonElement> entry : childrenJsonObject.entrySet()) {
            Either<Identifier, UnbakedModel> child = switch (entry.getValue()) {
                case JsonPrimitive reference -> Either.left(Identifier.parse(reference.getAsString()));
                case JsonObject inline -> Either.right(context.deserialize(inline, UnbakedModel.class));
                default -> throw new IllegalArgumentException("");
            };
            children.put(entry.getKey(), child);
        }
    }

}
