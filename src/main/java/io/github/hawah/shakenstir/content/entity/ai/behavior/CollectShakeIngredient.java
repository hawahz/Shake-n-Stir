package io.github.hawah.shakenstir.content.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import io.github.hawah.shakenstir.content.blockEntity.CabinetBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.SpiritContent;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class CollectShakeIngredient extends Behavior<BartenderEntity> {

    public static final boolean SHOULD_ADD_TO_INVENTORY = true;

    public CollectShakeIngredient() {
        super(
                ImmutableMap.of(
                        MemoryModuleType.VISITED_BLOCK_POSITIONS,
                        MemoryStatus.REGISTERED,
                        Memories.RECIPE.get(),
                        MemoryStatus.VALUE_PRESENT,
                        Memories.RECIPE_READY.get(),
                        MemoryStatus.VALUE_ABSENT
                )
        );
    }

    TakeUpItemTarget target;
    TransportItemState state = TransportItemState.TRAVELLING;
    List<ItemStack> wanderingItems = new ArrayList<>();
    List<FluidStack> wanderingFluids = new ArrayList<>();

    @Override
    protected boolean timedOut(long timestamp) {
        return false;
    }

    @Override
    protected void start(ServerLevel level, BartenderEntity body, long timestamp) {
        super.start(level, body, timestamp);
        body.getBrain().getMemory(Memories.RECIPE.get()).ifPresent(recipe -> {
            wanderingItems.clear();
            wanderingItems.addAll(recipe.requiredItems().stream().map(ItemStack::copy).toList());
            wanderingFluids.clear();
            wanderingFluids.addAll(recipe.requiredFluids().stream().map(FluidStack::copy).toList());
        });
        checkAndUpdateCarriedItem(body);
    }

    private void checkAndUpdateCarriedItem(BartenderEntity body) {
        NonNullList<ItemStack> inventory = body.getInventory();

        for (ItemStack itemStack : inventory) {
            if (itemStack.isEmpty()) {
                continue;
            }

            SpiritContent sc = itemStack.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, SpiritContent.EMPTY);

            if (!sc.isEmpty()) {
                FluidStack spiritFluid = sc.fluidStack();

                for (int j = 0; j < wanderingFluids.size(); j++) {
                    FluidStack required = wanderingFluids.get(j);

                    if (spiritFluid.is(required.getFluid())) {
                        int availableAmount = spiritFluid.getAmount();
                        int requiredAmount = required.getAmount();
                        int toReduce = Math.min(availableAmount, requiredAmount);

                        if (toReduce > 0) {
                            required.shrink(toReduce);
                            if (required.isEmpty()) {
                                wanderingFluids.remove(j);
                                j--;
                            }

                            break;
                        }
                    }
                }
            } else {
                for (int j = 0; j < wanderingItems.size(); j++) {
                    ItemStack required = wanderingItems.get(j);

                    if (ItemStack.isSameItemSameComponents(itemStack, required)) {
                        int availableCount = itemStack.getCount();
                        int requiredCount = required.getCount();
                        int toReduce = Math.min(availableCount, requiredCount);

                        if (toReduce > 0) {
                            required.shrink(toReduce);
                            if (required.isEmpty()) {
                                wanderingItems.remove(j);
                                j--;
                            }

                            break;
                        }
                    }
                }
            }
            if (wanderingFluids.isEmpty() && wanderingItems.isEmpty()) {
                break;
            }
        }
    }

    @Override
    protected void stop(ServerLevel level, BartenderEntity body, long timestamp) {
        body.getBrain().eraseMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS);
    }

    @Override
    protected void tick(ServerLevel level, BartenderEntity body, long timestamp) {
        if (wanderingItems.isEmpty() && wanderingFluids.isEmpty()) {
            body.getBrain().setMemory(Memories.RECIPE_READY.get(), Unit.INSTANCE);
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

        wanderingItems.removeIf(ItemStack::isEmpty);
        wanderingFluids.removeIf(FluidStack::isEmpty);
    }

    private int ticksSinceReachingTarget;
    enum InteractionState {
        SEARCH,
        PICK_UP
    }

    InteractionState interactionState = null;
    public static final int SEARCH_TIME = 20;

    private void onReachedTarget(TakeUpItemTarget target, ServerLevel level, BartenderEntity body) {
        if (!this.isWithinTargetDistance(1.0, target, level, body, body.getEyePosition())) {
            this.onStartTravelling(body);
        } else {
            this.ticksSinceReachingTarget++;
            if (this.ticksSinceReachingTarget >= SEARCH_TIME) {
                extractRequiredItemFromTarget(target, body);
                this.ticksSinceReachingTarget = 0;
                setVisitedBlockPos(body, body.level(), target.pos);
                interactionState = InteractionState.SEARCH;
                setTransportingState(TransportItemState.TRAVELLING);
                this.target = null;
            } else {
                body.getLookControl().setLookAt(target.pos.getCenter());
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

            ItemStack itemStack = resource.toStack(container.getAmountAsInt(i));
            SpiritContent sc = itemStack.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, SpiritContent.EMPTY);

            if (!sc.isEmpty()) {
                FluidStack spiritFluid = sc.fluidStack();

                for (int j = 0; j < wanderingFluids.size(); j++) {
                    FluidStack required = wanderingFluids.get(j);

                    if (spiritFluid.is(required.getFluid())) {
                        int requiredAmount = required.getAmount();
                        int availableInSlot = container.getAmountAsInt(i);
                        int toExtract = Math.min(availableInSlot, requiredAmount);

                        if (toExtract > 0) {
                            try (Transaction tx = Transaction.openRoot()) {
                                int extracted = container.extract(i, resource, toExtract, tx);

                                if (extracted > 0 && SHOULD_ADD_TO_INVENTORY) {
                                    ItemStack extractedStack = resource.toStack(extracted);

                                    OptionalInt emptySlot = findEmptyInventorySlot(body);
                                    if (emptySlot.isPresent()) {
                                        body.getInventory().set(emptySlot.getAsInt(), extractedStack);
                                        required.shrink(spiritFluid.getAmount());
                                        if (required.isEmpty()) {
                                            wanderingFluids.remove(j);
                                            j--;
                                        }
                                        tx.commit();
                                        body.swing(InteractionHand.MAIN_HAND);
                                    } else {
                                    }
                                }
                            }
                        }

                        if (wanderingFluids.isEmpty()) {
                            return;
                        }
                        break;
                    }
                }
            } else {
                for (int j = 0; j < wanderingItems.size(); j++) {
                    ItemStack required = wanderingItems.get(j);

                    if (ItemStack.isSameItemSameComponents(itemStack, required)) {
                        int requiredCount = required.getCount();
                        int availableCount = container.getAmountAsInt(i);
                        int toExtract = Math.min(availableCount, requiredCount);

                        if (toExtract > 0) {
                            try (Transaction tx = Transaction.openRoot()) {
                                int extracted = container.extract(i, resource, toExtract, tx);

                                if (extracted > 0 && SHOULD_ADD_TO_INVENTORY) {
                                    ItemStack extractedStack = resource.toStack(extracted);

                                    OptionalInt emptySlot = findEmptyInventorySlot(body);
                                    if (emptySlot.isPresent()) {
                                        body.getInventory().set(emptySlot.getAsInt(), extractedStack);
                                        required.shrink(extracted);
                                        if (required.isEmpty()) {
                                            wanderingItems.remove(j);
                                            j--;
                                        }
                                        tx.commit();
                                        body.swing(InteractionHand.MAIN_HAND);
                                    } else {
                                    }
                                }
                            }
                        }

                        if (wanderingItems.isEmpty()) {
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

    private void onStartTravelling(PathfinderMob body) {
//        this.onStartTravelling.accept(body);
        this.setTransportingState(TransportItemState.TRAVELLING);
        this.interactionState = null;
        this.ticksSinceReachingTarget = 0;
    }

    private void setTransportingState(TransportItemState state) {
        this.state = state;
    }

    private void onTravelToTarget(TakeUpItemTarget target, ServerLevel level, BartenderEntity body) {
        if (this.isWithinTargetDistance(getInteractionRange(body), target, level, body, body.getEyePosition())) {
            this.startOnReachedTargetInteraction(target, body);
        } else {
            this.walkTowardsTarget(body);
        }
    }

    private void startOnReachedTargetInteraction(TakeUpItemTarget target, BartenderEntity body) {
        this.setTransportingState(TransportItemState.INTERACTING);
    }

    private void walkTowardsTarget(PathfinderMob body) {
        if (this.target != null) {
            BehaviorUtils.setWalkAndLookTargetMemories(body, this.target.pos, 0.5F, 0);
        }
    }

    private static double getInteractionRange(PathfinderMob body) {
        return hasFinishedPath(body) ? 1.0 : 0.5;
    }

    private static boolean hasFinishedPath(PathfinderMob body) {
        return body.getNavigation().getPath() != null && body.getNavigation().getPath().isDone();
    }

    private boolean isWithinTargetDistance(
            double distance, TakeUpItemTarget target, Level level, BartenderEntity body, Vec3 fromPos
    ) {
        AABB boundingBox = body.getBoundingBox();
        AABB movedBoundBox = AABB.ofSize(fromPos, boundingBox.getXsize(), boundingBox.getYsize(), boundingBox.getZsize());
        return target.state.getCollisionShape(level, target.pos).bounds().inflate(distance, 0.5, distance).move(target.pos).intersects(movedBoundBox);
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

    public Optional<FluidStack> getMissingSpirit(BartenderEntity body) {
        NonNullList<ItemStack> inventory = body.getInventory();
        List<FluidStack> remainingFluids = inventory.stream()
                .map(itemStack -> itemStack.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, SpiritContent.EMPTY).fluidStack())
                .filter(fluidStack -> !fluidStack.isEmpty())
                .toList();

        return body.getBrain().getMemory(Memories.RECIPE.get()).map(
                recipe -> {
                    List<FluidStack> requirements = recipe.requiredFluids()
                            .stream()
                            .map(FluidStack::copy)
                            .toList();

                    for (FluidStack required : requirements) {
                        int requiredAmount = required.getAmount();
                        int availableAmount = 0;

                        for (FluidStack fluid : remainingFluids) {
                            if (fluid.is(required.getFluid())) {
                                availableAmount += fluid.getAmount();
                            }
                        }

                        if (availableAmount < requiredAmount) {
                            return Optional.of(required);
                        }
                    }

                    return Optional.<FluidStack>empty();
                }
        ).orElse(Optional.empty());
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

    @Override
    protected boolean canStillUse(ServerLevel level, BartenderEntity body, long timestamp) {
        return true;
    }

    public enum TransportItemState {
        TRAVELLING,
        INTERACTING;
    }

    public record TakeUpItemTarget(BlockPos pos, ResourceHandler<ItemResource> container, BlockEntity blockEntity, BlockState state) {
        public static CollectShakeIngredient.@Nullable TakeUpItemTarget tryCreatePossibleTarget(BlockEntity blockEntity, Level level) {
            BlockPos blockPos = blockEntity.getBlockPos();
            BlockState blockState = blockEntity.getBlockState();
            ResourceHandler<ItemResource> container = getBlockEntityContainer(blockEntity, blockState, level, blockPos);
            return container != null ? new CollectShakeIngredient.TakeUpItemTarget(blockPos, container, blockEntity, blockState) : null;
        }

        public static CollectShakeIngredient.@Nullable TakeUpItemTarget tryCreatePossibleTarget(BlockPos blockPos, Level level) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            return blockEntity == null ? null : tryCreatePossibleTarget(blockEntity, level);
        }

        public static CollectShakeIngredient.@Nullable TakeUpItemTarget tryGetCabinetDirectlyOrJustContainer(BlockEntity blockEntity, Level level) {
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
