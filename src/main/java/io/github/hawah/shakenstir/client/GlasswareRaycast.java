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
        Direction direction,
        boolean present
) {
    public static VoxelShape shape;
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
        return cache = new GlasswareRaycast(null, 0, null, false);
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

        Direction direction = RaycastHelper.intersectRayWithBox(
                worldBox,
                source,
                source.add(hitPos.subtract(source).multiply(10, 10, 10))
        );
        return cache = new GlasswareRaycast(localPosition, rotation, direction, true);
    }

    public static GlasswareRaycast cache = null;

    public static void clearCache() {
        cache = null;
    }

    public static VoxelShape getShape() {
        return shape;
    }
}
