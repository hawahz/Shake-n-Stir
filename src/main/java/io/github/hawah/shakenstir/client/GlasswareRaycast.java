package io.github.hawah.shakenstir.client;

import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.lib.RaycastHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector2f;

public record GlasswareRaycast(
        Vector2f localPosition,
        float rotation,
        float minX,
        float maxX,
        float minZ,
        float maxZ,
        float minY,
        float maxY,
        Direction direction,
        boolean present
) {
    public static GlasswareRaycast checkHitGlasswareDirect(Level level,
                                                           BlockPos blockPos,
                                                           Vec3 source,
                                                           Vec3 hitPos) {
        if (cache != null) {
            return cache;
        }
        if (level.getBlockEntity(blockPos) instanceof GlasswareBlockEntity blockEntity) {
            return checkHitGlasswareDirect(blockEntity, blockPos, source, hitPos);
        }
        return cache = new GlasswareRaycast(null, 0, 0, 0, 0, 0, 0, 0, null, false);
    }
    public static GlasswareRaycast checkHitGlasswareDirect(GlasswareBlockEntity blockEntity,
                                                           BlockPos blockPos,
                                                           Vec3 source,
                                                           Vec3 hitPos) {
        if (cache != null) {
            return cache;
        }
        Vector2f localPosition = blockEntity.position;
        float rotation = blockEntity.rotation; // rad


        double y = blockPos.getBottomCenter().y();
        int x = blockPos.getX();
        int z = blockPos.getZ();

        double v = 0.25;

        // 本地空间包围盒
        AABB localBox = new AABB(
                -v, 0, -v,
                v, 0.5, v
        );

        // 世界中心
        Vec3 center = new Vec3(
                x + localPosition.x,
                y,
                z + localPosition.y
        );

        AABB worldBox = localBox.move(center);

        float minX = (float) localBox.minX * 16;
        float maxX = (float) localBox.maxX * 16;
        float minZ = (float) localBox.minZ * 16;
        float maxZ = (float) localBox.maxZ * 16;
        float minY = (float) localBox.minY * 16;
        float maxY = (float) localBox.maxY * 16;


        Direction direction = RaycastHelper.intersectRayWithBox(
                worldBox,
                source,
                source.add(hitPos.subtract(source).multiply(10, 10, 10))
        );
        return cache = new GlasswareRaycast(localPosition, rotation, minX, maxX, minZ, maxZ, minY, maxY, direction, true);
    }

    public static VoxelShape getShape() {
        if (cache == null) {
            return Shapes.block();
        }
        return Block.box(
                cache.minX,
                cache.minY,
                cache.minZ,
                cache.maxX,
                cache.maxY,
                cache.maxZ
        );
    }

    public static GlasswareRaycast cache = null;

    public static void clearCache() {
        cache = null;
    }
}
