package io.github.hawah.shakenstir.content.entity;

import io.github.hawah.shakenstir.Config;
import io.github.hawah.shakenstir.content.block.BarCounterBlock;
import io.github.hawah.shakenstir.content.entity.ai.memory.BarData;
import io.github.hawah.shakenstir.foundation.tags.SnsBlockTags;
import io.github.hawah.shakenstir.lib.client.render.outliner.Outliner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.*;

public class BarAreaHelper {
    public static BarData calculateBarData(BoundingBox box, Level level) {


        // 所有可站立点
        Set<BlockPos> standable = new HashSet<>();

        // 先扫描整个 BoundingBox
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int y = box.minY(); y <= box.maxY(); y++) {
                for (int z = box.minZ(); z <= box.maxZ(); z++) {

                    BlockPos pos = new BlockPos(x, y, z);

                    if (checkPosValid(pos, level) == 0) {
                        standable.add(pos);
                    }
                }
            }
        }

        // BFS 连通块
        Set<BlockPos> visited = new HashSet<>();
        List<Platform> platforms = new ArrayList<>();

        for (BlockPos start : standable) {

            if (visited.contains(start)) {
                continue;
            }

            Platform platform = new Platform();

            Queue<BlockPos> queue = new ArrayDeque<>();
            queue.add(start);

            visited.add(start);

            while (!queue.isEmpty()) {

                BlockPos current = queue.poll();

                platform.blocks.add(current);

                for (Direction dir : Direction.Plane.HORIZONTAL) {

                    for (int dy = -1; dy <= 1; dy++) {

                        BlockPos next = current.relative(dir).offset(0, dy, 0);

                        if (!box.isInside(next)) {
                            continue;
                        }

                        if (!standable.contains(next)) {
                            continue;
                        }

                        if (visited.contains(next)) {
                            continue;
                        }

                        visited.add(next);
                        queue.add(next);
                    }
                }
            }

            platforms.add(platform);
        }

        Set<BlockPos> largestPlatform =
                platforms.stream()
                        .max(Comparator.comparingInt(Platform::size))
                        .map(Platform::getBlocks)
                        .orElse(Set.of());

        Set<BlockPos> counterCluster =
                findLargestCounterCluster(largestPlatform, level);

        Direction dir =
                findSweepDirection(counterCluster);

        Set<BlockPos> forward =
                sweepArea(counterCluster, largestPlatform, dir);

        Set<BlockPos> backward =
                sweepArea(counterCluster, largestPlatform, dir.getOpposite());

        Set<BlockPos> bartenderArea =
                forward.size() >= backward.size()
                        ? forward
                        : backward;

        if (level.isClientSide() && Config.Common.DEBUG_MODE.get()) {
            for (BlockPos blockPos : bartenderArea) {
                Outliner.getInstance().chaseBox(blockPos, blockPos, blockPos)
                        .lazyDiscard(500)
                        .finish();
            }
            for (BlockPos blockPos : counterCluster) {
                Outliner.getInstance().chaseBox(blockPos, blockPos, blockPos)
                        .lazyDiscard(500)
                        .setRGBA(1, 1, 0, 1)
                        .finish();
            }
        }
        return new BarData(counterCluster.stream().toList(), bartenderArea.stream().toList(), level.dimension());
    }

    public static int checkPosValid(BlockPos pos, Level level) {
        if (!level.getBlockState(pos).is(SnsBlockTags.BAR_AREA_IGNORED)) {
            return 1;
        } else if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos, Direction.UP)) {
            return -1;
        } else if (!level.getBlockState(pos.above()).is(SnsBlockTags.BAR_AREA_IGNORED)) {
            return 2;
        }
        return 0;
    }

    public static boolean isBarCounterValid(BlockPos barCounter, Level level) {
        return level.getBlockState(barCounter).getBlock() instanceof BarCounterBlock;
    }

    public static Set<BlockPos> findLargestCounterCluster(
            Set<BlockPos> platform,
            Level level
    ) {

        Set<BlockPos> counters = new HashSet<>();

        for (BlockPos pos : platform) {

            if (isBarCounterValid(pos.below(), level)) {
                counters.add(pos);
            }
        }

        Set<BlockPos> visited = new HashSet<>();

        Set<BlockPos> largest = new HashSet<>();

        for (BlockPos start : counters) {

            if (visited.contains(start)) {
                continue;
            }

            Set<BlockPos> cluster = new HashSet<>();

            Queue<BlockPos> queue = new ArrayDeque<>();

            queue.add(start);
            visited.add(start);

            while (!queue.isEmpty()) {

                BlockPos current = queue.poll();

                cluster.add(current);

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {

                        if (dx == 0 && dz == 0) {
                            continue;
                        }

                        BlockPos next =
                                current.offset(dx, 0, dz);

                        if (!counters.contains(next)) {
                            continue;
                        }

                        if (visited.contains(next)) {
                            continue;
                        }

                        visited.add(next);
                        queue.add(next);
                    }
                }
            }

            if (cluster.size() > largest.size()) {
                largest = cluster;
            }
        }

        return largest;
    }

    public static Direction findSweepDirection(
            Set<BlockPos> cluster
    ) {

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;

        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (BlockPos pos : cluster) {

            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());

            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        int xSpan = maxX - minX;
        int zSpan = maxZ - minZ;

        // 吧台沿 X 延伸
        // 工作区应该朝 Z sweep
        if (xSpan >= zSpan) {
            return Direction.NORTH;
        }

        // 吧台沿 Z 延伸
        // 工作区应该朝 X sweep
        return Direction.EAST;
    }

    public static Set<BlockPos> sweepArea(
            Set<BlockPos> line,
            Set<BlockPos> region,
            Direction dir
    ) {

        Set<BlockPos> frontier = new HashSet<>(line);

        Set<BlockPos> result = new HashSet<>(line);

        while (true) {

            Set<BlockPos> nextFrontier = new HashSet<>();

            for (BlockPos current : frontier) {

                for (int dy = -1; dy <= 1; dy++) {

                    BlockPos next =
                            current.relative(dir)
                                    .offset(0, dy, 0);

                    if (!region.contains(next)) {
                        continue;
                    }

                    if (result.contains(next)) {
                        continue;
                    }

                    nextFrontier.add(next);
                }
            }

            if (nextFrontier.isEmpty()) {
                break;
            }

            result.addAll(nextFrontier);

            frontier = nextFrontier;
        }
        result.removeAll(line);

        return result;
    }

    public static class Platform {

        public final Set<BlockPos> blocks = new HashSet<>();

        public Set<BlockPos> getBlocks() {
            return blocks;
        }
        public final int size() {
            return blocks.size();
        }
    }
}
