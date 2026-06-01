package io.github.hawah.shakenstir.content.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.SpiritContent;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.content.entity.ai.memory.BarData;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Comparator;
import java.util.Optional;

public class BartenderProduct extends Behavior<BartenderEntity> {

    public static final int SHAKING_DURATION = 100;
    private long endTime = -1;
    private BlockPos target = null;

    public BartenderProduct() {
        super(ImmutableMap.of(
                Memories.RECIPE.get(),
                MemoryStatus.VALUE_PRESENT,
                Memories.RECIPE_READY.get(),
                MemoryStatus.VALUE_PRESENT
        ));
    }

    enum State {
        APPROACHING_CUSTOMER,
        TURN_FOR_SHAKE,
        SHAKING,
        FINISH_SHAKING,
        POURING,
        END
    }

    State state = State.APPROACHING_CUSTOMER;

    private void setState(State state) {
        this.state = state;
    }

    @Override
    protected boolean timedOut(long timestamp) {
        return endTime > 0 && timestamp > endTime && state.equals(State.END);
    }

    @Override
    protected void start(ServerLevel level, BartenderEntity body, long timestamp) {
        endTime = -1;
        lookAtStamp = -1;
        setState(State.APPROACHING_CUSTOMER);
    }

    @Override
    protected void tick(ServerLevel level, BartenderEntity body, long timestamp) {

        if (state.equals(State.APPROACHING_CUSTOMER)) {
            this.doApproachingCustomer(body, timestamp);
        }
        if (state.equals(State.TURN_FOR_SHAKE)) {
            this.doTurnForShake(level, body, timestamp);
        }
        if (state.equals(State.SHAKING)) {
            if (!body.isShaking()) {
                body.startShaking();
            }
            if (body.getState().equals(BartenderEntity.AnimState.SHAKING)) {
                this.doShaking(level, body, timestamp);
            }
            if (endTime > 0 && timestamp > endTime) {
                setState(State.FINISH_SHAKING);
            }
        }
        if (state.equals(State.FINISH_SHAKING)) {
            setState(State.POURING);
        }
        if (state.equals(State.POURING)) {
//            body.getBrain().getMemory(Memories.BAR_MEMORY.get()).ifPresent(barData -> {
//                List<BlockPos> blockPos = barData.barCounter();
//                for (BlockPos blockPo : blockPos) {
//                    if (level.getBlockState(blockPo).isEmpty()) {
//
//                        break;
//                    }
//                }
//            });
            setState(State.END);
        }

    }

    private long lookAtStamp = -1;

    private void doTurnForShake(ServerLevel level, BartenderEntity body, long timestamp) {
        if (lookAtStamp < 0) {
            lookAtStamp = timestamp + 40L;
        } else if (timestamp > lookAtStamp) {
            body.setItemInHand(InteractionHand.MAIN_HAND, ItemRegistries.SHAKER.toStack());
            setState(State.SHAKING);
            return;
        }
        body.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET)
                .ifPresent(target -> {
                    Vec3 lookAt = target.position().subtract(body.position()).yRot(Mth.PI * 0.4F).add(body.position());
                    body.getLookControl().setLookAt(lookAt);
                    body.setYBodyRot(body.yHeadRot);
                });
    }

    private void doApproachingCustomer(BartenderEntity body, long timestamp) {
        if (target != null && isWithinTargetDistance(2.0, target, body)) {
            setState(State.TURN_FOR_SHAKE);
        } else {
            walkTowardsTarget(body);
        }
    }

    private void walkTowardsTarget(PathfinderMob body) {
        if (this.target == null) {
            BarData data = body.getBrain().getMemory(Memories.BAR_MEMORY.get()).orElseThrow();
            if (data.dimension() != body.level().dimension()) {
                return;
            }
            LivingEntity customer = body.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).orElseThrow();
            if (body.distanceTo(customer) <= 2) {
                return;
            }
            Optional<BlockPos> target = data.bartenderArea()
                    .stream()
                    .filter(pos ->
                            pos.closerThan(customer.blockPosition(), 2.5) &&
                                    !pos.closerThan(customer.blockPosition(), 1))
                    .min(Comparator.comparing(pos -> pos.distToCenterSqr(body.position())));
            target.ifPresent(
                    pos -> this.target = pos
            );
        }

        if (this.target != null) {
            BehaviorUtils.setWalkAndLookTargetMemories(body, this.target, 0.5F, 0);
        }
    }

    private boolean isWithinTargetDistance(
            double distance, BlockPos target, BartenderEntity body
    ) {
        return body.distanceToSqr(target.getCenter()) < distance * distance;
    }

    private void doShaking(ServerLevel level, BartenderEntity body, long timestamp) {
        if (endTime < 0) {
            endTime = timestamp + SHAKING_DURATION;
        }

    }

    @Override
    protected void stop(ServerLevel level, BartenderEntity body, long timestamp) {
        body.getBrain().eraseMemory(Memories.RECIPE_READY.get());
        body.getBrain().getMemory(Memories.RECIPE.get()).ifPresent(
                recipe -> {
                    System.out.println("Shaken stir: " + recipe.name());

                    NonNullList<ItemStack> inventory = body.getInventory();

                    for (ItemStack requiredItem : recipe.requiredItems()) {
                        int remainingCount = requiredItem.getCount();

                        for (int i = 0; i < inventory.size() && remainingCount > 0; i++) {
                            ItemStack slot = inventory.get(i);
                            if (slot.isEmpty()) {
                                continue;
                            }

                            if (ItemStack.isSameItemSameComponents(slot, requiredItem)) {
                                int availableCount = slot.getCount();
                                int toConsume = Math.min(availableCount, remainingCount);

                                slot.shrink(toConsume);
                                remainingCount -= toConsume;

                                if (slot.isEmpty()) {
                                    inventory.set(i, ItemStack.EMPTY);
                                }
                            }
                        }
                    }

                    for (FluidStack requiredFluid : recipe.requiredFluids()) {
                        int remainingAmount = requiredFluid.getAmount();

                        for (int i = 0; i < inventory.size() && remainingAmount > 0; i++) {
                            ItemStack slot = inventory.get(i);
                            if (slot.isEmpty()) {
                                continue;
                            }

                            SpiritContent spiritContent = slot.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, SpiritContent.EMPTY);
                            if (spiritContent.isEmpty()) {
                                continue;
                            }

                            FluidStack fluidInSlot = spiritContent.fluidStack();
                            if (!fluidInSlot.is(requiredFluid.getFluid())) {
                                continue;
                            }

                            int availableAmount = fluidInSlot.getAmount();
                            int toConsume = Math.min(availableAmount, remainingAmount);

                            if (toConsume > 0) {
                                FluidStack newFluid = fluidInSlot.copyWithAmount(fluidInSlot.getAmount() - toConsume);
                                slot.set(DataComponentTypeRegistries.SPIRIT_CONTENT, new SpiritContent(newFluid));
                                remainingAmount -= toConsume;

                                if (newFluid.isEmpty()) {
                                    inventory.set(i, ItemStack.EMPTY);
                                }
                            }
                        }
                    }

                }
        );
        body.setState(BartenderEntity.AnimState.DEFAULT);
        body.getBrain().eraseMemory(Memories.RECIPE.get());
        body.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, BartenderEntity body, long timestamp) {
        return true;
    }

    @Override
    public String debugString() {
        return "BartenderProduct[" + state.name() + "]";
    }
}
