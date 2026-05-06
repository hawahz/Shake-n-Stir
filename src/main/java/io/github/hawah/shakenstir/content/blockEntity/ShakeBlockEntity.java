package io.github.hawah.shakenstir.content.blockEntity;

import com.mojang.logging.LogUtils;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
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
    public static final int MAX_FLUID_CAPACITY = 16;
    public static final int MAX_HOLD_ITEMS = 6;

    private final ResourceHandler<FluidResource> fluidHandler = new ShakeFluidResourceResourceHandler();

    private final ShakeItemResourceResourceHandler itemHandler = new ShakeItemResourceResourceHandler();


    public ShakeBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityRegistries.SHAKE_BLOCK_ENTITY.get(), worldPosition, blockState);
    }

    public void reset() {
        Collections.fill(itemHandler.itemHolder, ItemStack.EMPTY);
    }

    public void putItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = itemHandler.insert(ItemResource.of(itemStack), 1, transaction);
//            itemStack.shrink(inserted);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, itemHandler.itemHolder);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag var4;
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), LogUtils.getLogger())) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            ContainerHelper.saveAllItems(output, itemHandler.itemHolder, true);
            var4 = output.buildResult();
        }

        return var4;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        ValueOutput.ValueOutputList fluid = output.childrenList("fluid");

        ShakeFluidResourceResourceHandler fluidHolder = (ShakeFluidResourceResourceHandler) fluidHandler;
        for (int i = 0; i < fluidHolder.size(); i++) {
            ValueOutput data = fluid.addChild();
            data.putString("fluid_id", fluidHolder.getResource(i).typeHolder().value().getFluidType().toString());
            data.putInt("amount", fluidHolder.fluidAmount.get(i));
        }

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
    }

    public NonNullList<ItemStack> getItemToRender() {
        return NonNullList.copyOf(itemHandler.itemHolder);
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

    protected class ShakeFluidResourceResourceHandler implements ResourceHandler<FluidResource> {

        public final NonNullList<FluidResource> fluidHolder = NonNullList.withSize(MAX_HOLD_FLUIDS, FluidResource.EMPTY);
        public final NonNullList<Integer> fluidAmount = NonNullList.withSize(MAX_HOLD_FLUIDS, 0);


        @Override
        public int size() {
            return MAX_HOLD_FLUIDS;// Sour, Sweet, Bitter, Alcohol, Bubbles, Juice, Ginger
        }

        @Override
        public FluidResource getResource(int index) {
            if (checkInValid(index)) {
                return FluidResource.EMPTY;
            }
            return fluidHolder.get(index);
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
            if (fluidHolder.get(index).isEmpty()) {
                fluidAmount.set(index, 0);
            }
            return fluidAmount.get(index);
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
            if (fluidHolder.size() >= MAX_HOLD_FLUIDS && !fluidHolder.contains(resource)) {
                return 0;
            }
            int validSlot = -1;
            int sum = 0;
            for (int i = 0; i < MAX_HOLD_FLUIDS; i++) {
                if (fluidHolder.get(i).isEmpty() || fluidHolder.get(i).equals(resource)) {
                    validSlot = i;
                }
                sum += fluidAmount.get(i);
            }
            int returnAmount = 0;
            if (validSlot != -1 && sum <= MAX_FLUID_CAPACITY) {
                fluidHolder.set(validSlot, resource);
                returnAmount = Math.min(amount, MAX_FLUID_CAPACITY - sum);
                fluidAmount.set(validSlot, fluidAmount.get(validSlot) + returnAmount);
            }
            return returnAmount;
        }
        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

    }
    protected class ShakeItemResourceResourceHandler implements ResourceHandler<ItemResource> {
        public final NonNullList<ItemStack> itemHolder = NonNullList.withSize(MAX_HOLD_ITEMS, ItemStack.EMPTY);
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
