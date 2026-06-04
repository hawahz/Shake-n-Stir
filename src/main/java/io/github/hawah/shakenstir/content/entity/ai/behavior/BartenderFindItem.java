package io.github.hawah.shakenstir.content.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import io.github.hawah.shakenstir.content.blockEntity.CabinetBlockEntity;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class BartenderFindItem extends Behavior<BartenderEntity> {
    public BartenderFindItem() {
        super(ImmutableMap.of(
                Memories.ITEM_TO_FIND.get(),                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.VISITED_BLOCK_POSITIONS,   MemoryStatus.REGISTERED,
                Memories.BAR_MEMORY.get(),                  MemoryStatus.VALUE_PRESENT
        ));
    }

    final List<Ingredient> itemToFind = new ArrayList<>();
    TakeUpItemTarget target;

    TransportItemState state = TransportItemState.TRAVELLING;
    public enum TransportItemState {
        TRAVELLING,
        INTERACTING;
    }

    InteractionState interactionState = null;
    enum InteractionState {
        SEARCH,
        PICK_UP
    }
    public static final int SEARCH_TIME = 20;

    @Override
    protected void start(ServerLevel level, BartenderEntity body, long timestamp) {
        itemToFind.clear();
        itemToFind.addAll(getItemToFind(body));
        body.getBrain().eraseMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS);
    }

    @Override
    protected void tick(ServerLevel level, BartenderEntity body, long timestamp) {
        if (itemToFind.isEmpty()) {
            this.doStop(level, body, timestamp);
            return;
        }
        boolean isTargetValidNow = pickTarget(level, body);
        if (target == null) {
            this.doStop(level, body, timestamp);
        } else if (isTargetValidNow) {
            if (state.equals(TransportItemState.TRAVELLING)) {
                this.onTravelToTarget(this.target, level, body);
            }

            if (state.equals(TransportItemState.INTERACTING)) {
                this.onReachedTarget(this.target, level, body);
            }
        }

//        itemToFind.removeIf(ItemStack::isEmpty);
    }

    private void onReachedTarget(TakeUpItemTarget target, ServerLevel level, BartenderEntity body) {
        if (!this.isWithinTargetDistance(1.0, target, level, body, body.getEyePosition())) {
            this.onStartTravelling(body);
        } else {
            this.ticksSinceReachingTarget++;
            body.getLookControl().setLookAt(target.pos.getCenter());
            if (this.ticksSinceReachingTarget >= SEARCH_TIME) {
                extractRequiredItemFromTarget(target, body);
                this.ticksSinceReachingTarget = 0;
                setVisitedBlockPos(body, body.level(), target.pos);
                interactionState = InteractionState.SEARCH;
                setTransportingState(TransportItemState.TRAVELLING);
                this.target = null;
            }
        }
    }

    private void extractRequiredItemFromTarget(TakeUpItemTarget target, BartenderEntity body) {
        ResourceHandler<ItemResource> container = target.container;
        int size = container.size();

        for (int i = 0; i < size; i++) {
            ItemResource resource = container.getResource(i);
            if (resource.isEmpty()) {
                continue;
            }

            ItemStack itemStack = resource.toStack(container.getAmountAsInt(i));{
            for (int j = 0; j < itemToFind.size(); j++) {
                Ingredient required = itemToFind.get(j);

                if (required.test(itemStack)) {
                    int requiredCount = 1;
                    int availableCount = container.getAmountAsInt(i);
                    int toExtract = Math.min(availableCount, requiredCount);

                    if (toExtract > 0) {
                        try (Transaction tx = Transaction.openRoot()) {
                            int extracted = container.extract(i, resource, toExtract, tx);

                            if (extracted > 0) {
                                ItemStack extractedStack = resource.toStack(extracted);

                                OptionalInt emptySlot = findEmptyInventorySlot(body);
                                if (emptySlot.isPresent()) {
                                    body.getInventory().set(emptySlot.getAsInt(), extractedStack);
                                    itemToFind.remove(j);
                                    tx.commit();
                                    body.swing(InteractionHand.MAIN_HAND);
                                }
                            }
                        }
                    }

                    if (itemToFind.isEmpty()) {
                        return;
                    }
                    break;
                }
            }
            }
        }
    }

    private OptionalInt findEmptyInventorySlot(BartenderEntity body) {
        NonNullList<ItemStack> inventory = body.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).isEmpty()) {
                return OptionalInt.of(i);
            }
        }
        return OptionalInt.empty();
    }

    protected void setVisitedBlockPos(PathfinderMob body, Level level, BlockPos target) {
        Set<GlobalPos> visitedPositions = new HashSet<>(getVisitedPositions(body));
        visitedPositions.add(new GlobalPos(level.dimension(), target));
        if (visitedPositions.size() > 50) {
//            this.enterCooldownAfterNoMatchingTargetFound(body);
        } else {
            body.getBrain().setMemoryWithExpiry(MemoryModuleType.VISITED_BLOCK_POSITIONS, visitedPositions, 6000L);
        }
    }

    private static Set<GlobalPos> getVisitedPositions(PathfinderMob mob) {
        return mob.getBrain().getMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS).orElse(Set.of());
    }


    private int ticksSinceReachingTarget = 0;
    private void onStartTravelling(PathfinderMob body) {
//        this.onStartTravelling.accept(body);
        this.setTransportingState(TransportItemState.TRAVELLING);
        this.interactionState = null;
        this.ticksSinceReachingTarget = 0;
    }


    private void onTravelToTarget(TakeUpItemTarget target, ServerLevel level, BartenderEntity body) {
        if (this.isWithinTargetDistance(getInteractionRange(body), target, level, body, body.getEyePosition())) {
            this.startOnReachedTargetInteraction(target, body);
        } else {
            this.walkTowardsTarget(body);
        }
    }

    private void walkTowardsTarget(PathfinderMob body) {
        if (this.target != null) {
            BehaviorUtils.setWalkAndLookTargetMemories(body, this.target.pos, 0.5F, 0);
        }
    }

    long interactionDuration = 0;

    private void startOnReachedTargetInteraction(TakeUpItemTarget target, BartenderEntity body) {
        this.setTransportingState(TransportItemState.INTERACTING);
        interactionDuration = -1;
    }

    private void setTransportingState(TransportItemState state) {
        this.state = state;
    }

    private boolean isWithinTargetDistance(
            double distance, TakeUpItemTarget target, Level level, BartenderEntity body, Vec3 fromPos
    ) {
        AABB boundingBox = body.getBoundingBox();
        AABB movedBoundBox = AABB.ofSize(fromPos, boundingBox.getXsize(), boundingBox.getYsize(), boundingBox.getZsize());
        return target.state.getCollisionShape(level, target.pos).bounds().inflate(distance, body.getEyeY(), distance).move(target.pos).intersects(movedBoundBox);
    }

    private static double getInteractionRange(PathfinderMob body) {
        return hasFinishedPath(body) ? 1.2 : 1.0;
    }

    private static boolean hasFinishedPath(PathfinderMob body) {
        return body.getNavigation().getPath() != null && body.getNavigation().getPath().isDone();
    }

    @Override
    protected void stop(ServerLevel level, BartenderEntity body, long timestamp) {
        body.getBrain().eraseMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS);
        body.getBrain().eraseMemory(Memories.ITEM_TO_FIND.get());
    }

    @Override
    protected boolean canStillUse(ServerLevel level, BartenderEntity body, long timestamp) {
        return true;
    }

    @Override
    protected boolean timedOut(long timestamp) {
        return false;
    }

    @Override
    public String debugString() {
        return super.debugString();
    }

    public static List<Ingredient> getItemToFind(PathfinderMob mob) {
        return mob.getBrain().getMemory(Memories.ITEM_TO_FIND.get()).orElse(List.of());
    }
    public boolean pickTarget(ServerLevel level, BartenderEntity body) {
        if (target != null) {
            return target.blockEntity.equals(level.getBlockEntity(target.pos()));
        }
        var barData = body.getBrain().getMemory(Memories.BAR_MEMORY.get()).orElse(null);
        if (barData == null) return false;
        var area = barData.bartenderArea();

        var expandedArea = new HashSet<BlockPos>();
        for (var pos : area) {
            for (int dx = -5; dx <= 5; dx++) {
                for (int dy = -5; dy <= 5; dy++) {
                    for (int dz = -5; dz <= 5; dz++) {
                        expandedArea.add(pos.offset(dx, dy, dz));
                    }
                }
            }
        }

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (var pos : area) {
            minX = Math.min(minX, pos.getX() - 5);
            maxX = Math.max(maxX, pos.getX() + 5);
            minZ = Math.min(minZ, pos.getZ() - 5);
            maxZ = Math.max(maxZ, pos.getZ() + 5);
        }

        var bodyPos = body.blockPosition();
        record Candidate(TakeUpItemTarget target, double distSqr) {}
        var candidates = new ArrayList<Candidate>();

        for (int cx = minX >> 4; cx <= maxX >> 4; cx++) {
            for (int cz = minZ >> 4; cz <= maxZ >> 4; cz++) {
                var chunkAt = level.getChunkSource().getChunkNow(cx, cz);
                if (chunkAt == null) continue;
                for (var blockEntity : chunkAt.getBlockEntities().values()) {
                    var bePos = blockEntity.getBlockPos();
                    if (!expandedArea.contains(bePos)) continue;
                    TakeUpItemTarget tar;
                    if ((tar = TakeUpItemTarget.tryGetCabinetDirectlyOrJustContainer(blockEntity, level)) != null && isTargetValid(tar, body, level)) {
                        candidates.add(new Candidate(tar, bePos.distSqr(bodyPos)));
                    }
                }
            }
        }

        candidates.sort(Comparator.comparingDouble(Candidate::distSqr));
        if (!candidates.isEmpty()) {
            this.target = candidates.getFirst().target;
            return true;
        }
        return false;
    }

    public boolean isTargetValid(TakeUpItemTarget target, BartenderEntity body, Level level) {
        return body.getBrain().getMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS).map(
                visited -> !visited.contains(new GlobalPos(level.dimension(), target.pos()))
        ).orElse(true);
    }


    public record TakeUpItemTarget(BlockPos pos, ResourceHandler<ItemResource> container, BlockEntity blockEntity, BlockState state) {
        public static @Nullable TakeUpItemTarget tryCreatePossibleTarget(BlockEntity blockEntity, Level level) {
            BlockPos blockPos = blockEntity.getBlockPos();
            BlockState blockState = blockEntity.getBlockState();
            ResourceHandler<ItemResource> container = getBlockEntityContainer(blockEntity, blockState, level, blockPos);
            return container != null ? new TakeUpItemTarget(blockPos, container, blockEntity, blockState) : null;
        }

        public static @Nullable TakeUpItemTarget tryCreatePossibleTarget(BlockPos blockPos, Level level) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            return blockEntity == null ? null : tryCreatePossibleTarget(blockEntity, level);
        }

        public static @Nullable TakeUpItemTarget tryGetCabinetDirectlyOrJustContainer(BlockEntity blockEntity, Level level) {
            if (blockEntity instanceof CabinetBlockEntity be) {
                return new TakeUpItemTarget(be.getBlockPos(), be.itemHandler, blockEntity, blockEntity.getBlockState());
            }
            return tryCreatePossibleTarget(blockEntity.getBlockPos(), level);
        }

        private static @Nullable ResourceHandler<ItemResource> getBlockEntityContainer(BlockEntity blockEntity, BlockState blockState, Level level, BlockPos blockPos) {
            ResourceHandler<ItemResource> cap;
            if ((cap = level.getCapability(Capabilities.Item.BLOCK, blockPos, blockState, blockEntity, Direction.NORTH)) != null) {
                return cap;
            }
            return null;
        }
    }
}
