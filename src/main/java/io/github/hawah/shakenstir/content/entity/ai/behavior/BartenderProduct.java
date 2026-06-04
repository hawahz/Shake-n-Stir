package io.github.hawah.shakenstir.content.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.content.data.SnsRecipeHolder;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.SpiritContent;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.content.entity.ai.memory.BarData;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import io.github.hawah.shakenstir.content.item.GlasswareItem;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Vector2f;

import java.util.*;

public class BartenderProduct extends Behavior<BartenderEntity> {

    public static final int SHAKING_DURATION = 100;
    public static final int DECORATING_DURATION = 10;
    public static final int POURING_DURATION = 20;
    public static final int POURING_CD = 20;
    public static final long LOOKAT_DURATION = 40L;
    private long endTime = -1;
    private BlockPos target = null;

    public BartenderProduct() {
        super(ImmutableMap.of(
                Memories.RECIPE.get(),
                MemoryStatus.VALUE_PRESENT,
                Memories.RECIPE_READY.get(),
                MemoryStatus.VALUE_PRESENT,
                Memories.MEMORY_GLASSWARE.get(),
                MemoryStatus.REGISTERED
        ));
    }

    enum State {
        APPROACHING_CUSTOMER,
        TURN_FOR_SHAKE,
        SHAKING,
        FINISH_SHAKING,
        POURING,
        DECORATING,
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
    SnsRecipeHolder recipeHolder;
    long glasswareFindTimeout = 0;
    @Override
    protected void start(ServerLevel level, BartenderEntity body, long timestamp) {
        endTime = -1;
        lookAtStamp = -1;
        pouringTimeout = -1;
        pouringCD = -1;
        decoratingTimeout = -1;
        decoratingPlaceTimeout = -1;
        placePos = null;
        setState(State.APPROACHING_CUSTOMER);
        body.getBrain().getMemory(Memories.RECIPE.get()).ifPresent(recipe -> {
            recipeHolder = recipe;
        });
        decorations.clear();
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
            this.doShaking(level, body, timestamp);
        }
        if (state.equals(State.FINISH_SHAKING)) {
            this.prepareForPouring(body, timestamp);
        }
        if (state.equals(State.POURING)) {
            this.doPouring(level, body, timestamp);
        }
        if (state.equals(State.DECORATING)) {
            this.doDecorating(level, body, timestamp);
        }
    }
    List<GlasswareBlockEntity.Decoration> decorations = new ArrayList<>();
    long decoratingTimeout = -1;
    long decoratingPlaceTimeout = -1;
    private void doDecorating(ServerLevel level, BartenderEntity body, long timestamp) {
        if (decorations.isEmpty() && body.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            decorations.addAll(recipeHolder.decorations());
            if (decorations.isEmpty()) {
                setState(State.END);
                return;
            }
        }
        if (body.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            GlasswareBlockEntity.Decoration deco = decorations.getFirst();
            if (deco.itemStack().isEmpty()) {
                return;
            }
            NonNullList<ItemStack> inventory = body.getInventory();
            for (int i = 0, inventorySize = inventory.size(); i < inventorySize; i++) {
                ItemStack itemStack = inventory.get(i);
                if (itemStack.is(deco.itemStack().getItem())) {
                    body.setItemInHand(InteractionHand.MAIN_HAND, deco.itemStack().copy());
                    body.setInventorySlot(i, ItemStack.EMPTY);
                    break;
                }
            }
        }
        if (decoratingTimeout < 0) {
            decoratingTimeout = timestamp + DECORATING_DURATION + level.getRandom().nextInt(3);
        }
        if (decoratingTimeout >= timestamp) {
            return;
        }
        decoratingTimeout = timestamp + DECORATING_DURATION + level.getRandom().nextInt(3);
        body.getBrain().getMemory(Memories.MEMORY_GLASSWARE.get()).ifPresent(
                glassware -> {
                    if (level.getBlockEntity(glassware.pos()) instanceof GlasswareBlockEntity glasswareBlockEntity){
                        GlasswareBlockEntity.Decoration deco = decorations.getFirst();
                        glasswareBlockEntity.insertDecoration(deco);
                    }
                }
        );
        body.swing(InteractionHand.MAIN_HAND);
        if (!decorations.isEmpty()){
            decorations.removeFirst();
        }
        body.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
        if (body.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            body.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        if (decorations.isEmpty()) {
            setState(State.END);
        }
    }

    long pouringCD = -1;
    long pouringTimeout = -1;
    BlockPos placePos;
    private void doPouring(ServerLevel level, BartenderEntity body, long timestamp) {
        ItemStack itemInHand = body.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemInHand.getItem() instanceof GlasswareItem glasswareItem) {
            if (pouringCD < timestamp) {
                Vector2f localPos = new Vector2f(level.getRandom().nextFloat() % 1 - 0.5F, level.getRandom().nextFloat() % 1 - 0.5F);
                itemInHand.set(DataComponentTypeRegistries.GLASSWARE_ROTATION, body.getYRot() + 45);
                UseOnContext useOnContext = new UseOnContext(
                        level,
                        new FakePlayer(level, new GameProfile(UUID.randomUUID(), "bartender")),
                        InteractionHand.MAIN_HAND,
                        itemInHand,
                        new BlockHitResult(
                                placePos.getCenter().add(localPos.x(), 0.5, localPos.y()),
                                Direction.UP,
                                placePos,
                                false
                        )
                );
                glasswareItem.useOn(useOnContext);
                body.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                body.getBrain().setMemory(Memories.MEMORY_GLASSWARE.get(), new GlobalPos(level.dimension(), placePos));
                pouringTimeout = timestamp + POURING_CD;
            } else if (placePos == null) {
                body.getBrain().getMemory(Memories.BAR_MEMORY.get()).ifPresent(barData -> {
                    List<BlockPos> counters = barData.barCounter();
                    counters.stream()
                            .filter(pos -> level.getBlockState(pos).isEmpty())
                            .min(Comparator.comparing(bp -> body.distanceToSqr(bp.getCenter())))
                            .ifPresent(blockPos -> {
                                placePos = blockPos;
                            });
                });
            } else {
                BehaviorUtils.setWalkAndLookTargetMemories(body, placePos, 0.5F, 2);
                body.getLookControl().setLookAt(placePos.getCenter());
            }
            return;
        }
        if (pouringTimeout < timestamp && pouringTimeout > 0) {
            body.getBrain().getMemory(Memories.MEMORY_GLASSWARE.get()).ifPresent(
                    glassware -> {
                        if (level.getBlockEntity(glassware.pos()) instanceof GlasswareBlockEntity glasswareBlockEntity){
                            recipeHolder.recipe().apply(
                                    glasswareBlockEntity,
                                    recipeHolder.result()
                            );
                        }
                    }
            );
            body.swing(InteractionHand.MAIN_HAND);
            setState(State.DECORATING);
            return;
        } else if (pouringTimeout > 0) {
            return;
        }
        int glasswareIdx = -1;
        for (int i = 0; i < body.getInventory().size(); i++) {
            if (body.getInventorySlot(i).getItem() instanceof GlasswareItem) {
                glasswareIdx = i;
                break;
            }
        }
        if (glasswareIdx < 0) {
            setState(State.END);
        }
        pouringCD = timestamp + POURING_DURATION;
        ItemStack shortGlass = GlasswareItem.getShortGlass(ShakenStir.asResource(recipeHolder.holderGlass()));
        body.setItemInHand(InteractionHand.MAIN_HAND, shortGlass);
        body.setInventorySlot(glasswareIdx, ItemStack.EMPTY);
    }

    long prepareWalkTimeout = -1;
    public static final int PREPARE_WALK_DURATION = 20;
    private void prepareForPouring(BartenderEntity body, long timestamp) {
        if (glasswareFindTimeout < timestamp) {
            setState(State.END);
        }


        if (body.getBrain().checkMemory(Memories.ITEM_TO_FIND.get(), MemoryStatus.VALUE_ABSENT)) {
            if (prepareWalkTimeout < 0) {
                prepareWalkTimeout = timestamp + PREPARE_WALK_DURATION;
            }

            if (prepareWalkTimeout < timestamp) {
                setState(State.POURING);
            }
        }
    }

    private long lookAtStamp = -1;

    private void doTurnForShake(ServerLevel level, BartenderEntity body, long timestamp) {
        if (lookAtStamp < 0) {
            lookAtStamp = timestamp + LOOKAT_DURATION;
        } else if (timestamp > lookAtStamp) {
            body.setItemInHand(InteractionHand.MAIN_HAND, ItemRegistries.SHAKER.toStack());
            setState(State.SHAKING);
            return;
        }
        body.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET)
                .ifPresent(target -> {
                    Vec3 lookAt = target.getEyePosition().subtract(body.getEyePosition()).yRot(Mth.PI * 0.4F).add(body.position());
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
        if (!body.isShaking()) {
            body.startShaking();
        }
        if (body.getState().equals(BartenderEntity.AnimState.SHAKING)) {
            if (endTime < 0) {
                endTime = timestamp + SHAKING_DURATION;
            }
        }
        if (endTime > 0 && timestamp > endTime) {
            setState(State.FINISH_SHAKING);
            body.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            body.setState(BartenderEntity.AnimState.DEFAULT);
            if (recipeHolder != null) {
                body.getBrain().setMemory(Memories.ITEM_TO_FIND.get(), recipeHolder.getItemToFind());
                glasswareFindTimeout = timestamp + 5 * 20 * 60;
            }
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
