package io.github.hawah.shakenstir.content.blockEntity;

import com.mojang.logging.LogUtils;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.content.block.Shake;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.FluidStackDataComponent;
import io.github.hawah.shakenstir.content.dataComponent.ShakeFluidDataComponent;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import io.github.hawah.shakenstir.util.FluidStackWithSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ShakeBlockEntity extends BlockEntity implements ItemOwner {
    public static final int MAX_HOLD_FLUIDS = 6;
    public static final int MAX_FLUID_CAPACITY = 1000;
    public static final int MAX_HOLD_ITEMS = 4;

    public float animationHeight = 0;
    public float oAnimationHeight = 0;

    private final ShakeFluidResourceResourceHandler fluidHandler = new ShakeFluidResourceResourceHandler();
    private final ShakeItemResourceResourceHandler itemHandler = new ShakeItemResourceResourceHandler();


    public ShakeBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityRegistries.SHAKE_BLOCK_ENTITY.get(), worldPosition, blockState);
    }

    public void reset() {
        itemHandler.itemHolder.clear();
        fluidHandler.fluidHolder.clear();
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ShakeBlockEntity blockEntity) {
        if (level.isClientSide()) {
            blockEntity.oAnimationHeight = blockEntity.animationHeight;
            blockEntity.animationHeight = Mth.lerp(ShakenStirClient.ANI_DELTAF * 0.3F, blockEntity.animationHeight, blockEntity.getFluidAmount() / 1000F);
            if (!state.getValue(Shake.FACING).equals(Direction.DOWN)) {
                blockEntity.animationHeight = 0;
                blockEntity.oAnimationHeight = 0;
            }
        }
    }

    public boolean holdingProduct() {
        return itemHandler.getResource(0).is(ItemRegistries.CONTENT_HOLDER.get());
    }

    public ItemStack getProduct() {
        if (!holdingProduct()) {
            return ItemStack.EMPTY;
        }
        return itemHandler.getResource(0).toStack();
    }

    public NonNullList<ItemStack> getItemToRender() {
        if (holdingProduct()) {
            return NonNullList.withSize(itemHandler.size(), ItemStack.EMPTY);
        }
        return NonNullList.copyOf(itemHandler.itemHolder);
    }

    public boolean putItem(ItemStack itemStack, boolean isCreative) {
        if (itemStack.isEmpty() || holdingProduct()) {
            return false;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = itemHandler.insert(ItemResource.of(itemStack), 1, transaction);
            if (!isCreative) {
                itemStack.shrink(inserted);
            }
            return inserted != 0;
        }
    }

    public ItemStack popItem() {
        try (Transaction transaction = Transaction.openRoot()) {
            int top;
            ItemResource resource = ItemResource.EMPTY;
            for (top = itemHandler.size() - 1; top >= 0; top--) {
                if (!(resource = itemHandler.getResource(top)).isEmpty() && !resource.is(ItemRegistries.CONTENT_HOLDER.get())) {
                    break;
                }
            }
            if (top == -1 || resource.isEmpty() || resource.is(ItemRegistries.CONTENT_HOLDER.get())) {
                return ItemStack.EMPTY;
            }
            int inserted = itemHandler.extract(resource, 1, transaction);
            return inserted > 0 ? resource.toStack(inserted) : ItemStack.EMPTY;
        }
    }

    public boolean pourLiquid(FluidStack fluid, boolean isCreative) {
        if (fluid.isEmpty() || holdingProduct()) {
            return false;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = fluidHandler.insert(FluidResource.of(fluid), 250, transaction);
            if (!isCreative) {
                fluid.shrink(inserted);
            }
            return inserted != 0;
        }
    }

    public int getFluidAmount() {
        if (holdingProduct()) {
            return 1000;
        }
        int sum = 0;
        for (int i = 0; i < fluidHandler.size(); i++) {
            sum += fluidHandler.getAmountAsInt(i);
        }
        return sum;
    }

    public NonNullList<FluidStack> getFluidStack() {
        NonNullList<FluidStack> list = NonNullList.create();
        for (int i = 0; i < fluidHandler.size(); i++) {
            if (!fluidHandler.fluidHolder.get(i).isEmpty()) {
                list.add(fluidHandler.fluidHolder.get(i));
            }
        }
        return list;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, itemHandler.itemHolder);
        loadAllFluids(input, fluidHandler.fluidHolder);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag var4;
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), LogUtils.getLogger())) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            ContainerHelper.saveAllItems(output, itemHandler.itemHolder, true);
            saveAllFluids(output, fluidHandler.fluidHolder, false);
            var4 = output.buildResult();
        }

        return var4;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        saveAllFluids(output, fluidHandler.fluidHolder, true);
        ContainerHelper.saveAllItems(output, itemHandler.itemHolder, true);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        ItemContainerContents itemContents = components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        itemHandler.itemHolder.clear();
        int slots = itemContents.getSlots();
        for (int i = 0; i < Math.min(slots, MAX_HOLD_ITEMS); i++) {
            itemHandler.itemHolder.set(i, itemContents.getStackInSlot(i));
        }
        ShakeFluidDataComponent shakeFluidData = components.getOrDefault(DataComponentTypeRegistries.SHAKE_CONTENT, ShakeFluidDataComponent.EMPTY);
        fluidHandler.fluidHolder.clear();
        for (int i = 0; i < Math.min(shakeFluidData.size(), MAX_HOLD_FLUIDS); i++) {
            fluidHandler.fluidHolder.set(i, shakeFluidData.fluidStacks().get(i));
        }

    }

    @Override
    public Level level() {
        return getLevel();
    }

    @Override
    public Vec3 position() {
        return getBlockPos().getCenter();
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return 0;
    }

    public static void saveAllFluids(ValueOutput output, NonNullList<FluidStack> fluidStacks, boolean alsoWhenEmpty) {
        ValueOutput.TypedOutputList<FluidStackWithSlot> fluidsOutput = output.list("Fluids", FluidStackWithSlot.CODEC);

        for (int i = 0; i < fluidStacks.size(); i++) {
            FluidStack fluidStack = fluidStacks.get(i);
            if (!fluidStack.isEmpty()) {
                fluidsOutput.add(new FluidStackWithSlot(i, fluidStack));
            }
        }

        if (fluidsOutput.isEmpty() && !alsoWhenEmpty) {
            output.discard("Fluids");
        }
    }

    public static void loadAllFluids(ValueInput input, NonNullList<FluidStack> fluidStacks) {
        for (FluidStackWithSlot item : input.listOrEmpty("Fluids", FluidStackWithSlot.CODEC)) {
            if (item.isValidInContainer(fluidStacks.size())) {
                fluidStacks.set(item.slot(), item.stack());
            }
        }
    }

    protected class ShakeFluidResourceResourceHandler implements ResourceHandler<FluidResource> {

        public final NonNullList<FluidStack> fluidHolder = NonNullList.withSize(MAX_HOLD_FLUIDS, FluidStack.EMPTY);


        @Override
        public int size() {
            return MAX_HOLD_FLUIDS;// Sour, Sweet, Bitter, Alcohol, Bubbles, Juice, Ginger
        }

        @Override
        public FluidResource getResource(int index) {
            if (checkInValid(index)) {
                return FluidResource.EMPTY;
            }
            return FluidResource.of(fluidHolder.get(index));
        }

        private boolean checkInValid(int index) {
            if (index >= size() || index < 0) {
                LogUtils.getLogger().warn("Invalid index: {}", index);
                return true;
            }
            return false;
        }

        @Override
        public long getAmountAsLong(int index) {
            if (checkInValid(index)) {
                return 0L;
            }
            return fluidHolder.get(index).getAmount();
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource) {
            return MAX_FLUID_CAPACITY;
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return true;
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (fluidHolder.size() >= MAX_HOLD_FLUIDS && !fluidHolder.contains(resource) && !fluidHolder.contains(FluidStack.EMPTY)) {
                return 0;
            }
            int validSlot = -1;
            int sum = 0;
            for (int i = 0; i < MAX_HOLD_FLUIDS; i++) {
                if ((fluidHolder.get(i).isEmpty() && validSlot < 0) || resource.is(fluidHolder.get(i).getFluid())) {
                    validSlot = i;
                }
                sum += fluidHolder.get(i).getAmount();
            }
            int returnAmount = 0;
            if (validSlot != -1 && sum <= MAX_FLUID_CAPACITY) {
                returnAmount = Math.min(amount, MAX_FLUID_CAPACITY - sum);
                if (fluidHolder.get(validSlot).isEmpty()) {
                    fluidHolder.set(validSlot, resource.toStack(returnAmount));
                } else {
                    FluidStack fluidStack = fluidHolder.get(validSlot);
                    fluidStack.setAmount(returnAmount + fluidStack.getAmount());
                }
            }
            markChanged();
            return returnAmount;
        }
        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (amount == 0) {
                return 0;
            }
            markChanged();
            return 0;
        }

    }
    protected class ShakeItemResourceResourceHandler implements ResourceHandler<ItemResource> {
        public final NonNullList<ItemStack> itemHolder = NonNullList.withSize(MAX_HOLD_ITEMS, ItemStack.EMPTY);
        public final NonNullList<Float> itemInsertTime = NonNullList.withSize(MAX_HOLD_ITEMS, -1F);
        @Override
        public int size() {
            return MAX_HOLD_ITEMS;
        }

        @Override
        public ItemResource getResource(int index) {
            if (checkInValid(index)) {
                return ItemResource.EMPTY;
            }
            return ItemResource.of(itemHolder.get(index));
        }

        private boolean checkInValid(int index) {
            if (index >= size() || index < 0) {
                LogUtils.getLogger().warn("Invalid index: {}", index);
                return true;
            }
            return false;
        }

        @Override
        public long getAmountAsLong(int index) {
            if (checkInValid(index)) {
                return 0L;
            }
            return 1L;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            if (checkInValid(index)) {
                return 0L;
            }
            return 1;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return !checkInValid(index);
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (amount == 0) {
                return 0;
            }
            int stackBottom = 0;
            while (stackBottom < itemHolder.size()) {
                if (itemHolder.get(stackBottom).isEmpty()) {
                    itemHolder.set(stackBottom, resource.toStack());
                    markChanged();
                    if (level().isClientSide()){
                        itemInsertTime.set(stackBottom, AnimationTickHolder.getRenderTime());
                    }
                    return 1;
                }
                stackBottom++;
            }
            markChanged();
            return 0;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (amount == 0) {
                return 0;
            }
            int stackTop = itemHolder.size() - 1;
            while (stackTop >= 0) {
                if (ItemResource.of(itemHolder.get(stackTop)).equals(resource)) {
                    itemHolder.set(stackTop, ItemStack.EMPTY);
                    markChanged();
                    return 1;
                }
                if (!itemHolder.get(stackTop).isEmpty()) {
                    return 0;
                }
                stackTop--;
            }
            markChanged();
            return 1;
        }
    }

    protected void markChanged() {
        setChanged(getLevel(), getBlockPos(), getBlockState());
    }
}
