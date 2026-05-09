package io.github.hawah.shakenstir.content.blockEntity;

import com.mojang.logging.LogUtils;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.content.block.SpiritBlock;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.FluidStackDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SpiritBlockEntity extends BlockEntity {

    public NonNullList<FluidStack> fluidStacks = NonNullList.withSize(4, FluidStack.EMPTY);

    public SpiritBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityRegistries.SPIRIT_BLOCK_ENTITY.get(), worldPosition, blockState);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        if (getBlockState().getValue(SpiritBlock.COUNTS) != 1) {
            return;
        }
        FluidStackDataComponent fluidData = components.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, FluidStackDataComponent.EMPTY);
        fluidStacks.set(0, fluidData.fluidStack());
        setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ShakeBlockEntity.loadAllFluids(input, fluidStacks);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag var4;
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), LogUtils.getLogger())) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            ShakeBlockEntity.saveAllFluids(output, fluidStacks, false);
            var4 = output.buildResult();
        }

        return var4;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ShakeBlockEntity.saveAllFluids(output, fluidStacks, true);
    }

    public NonNullList<FluidStack> getFluidStacks() {
        return fluidStacks;
    }

    public void pushAnother(int index, ItemStack itemStack, boolean isCreative) {
        FluidStack fluidStack = itemStack.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, FluidStackDataComponent.EMPTY).fluidStack();
        fluidStacks.set(index, fluidStack);
        setChanged();
        if (!isCreative) {
            itemStack.shrink(1);
        }

    }

    public ResourceHandler<FluidResource> getFluidHandler() {
        return fluidHandler;
    }

    private final ResourceHandler<FluidResource> fluidHandler = new ResourceHandler<>() {
        @Override
        public int size() {
            return getBlockState().getValue(SpiritBlock.COUNTS);
        }

        @Override
        public FluidResource getResource(int index) {
            if (index >= size()) {
                return FluidResource.EMPTY;
            }
            return FluidResource.of(fluidStacks.get(index));
        }

        @Override
        public long getAmountAsLong(int index) {
            if (index >= size()) {
                return 0;
            }
            return fluidStacks.get(index).getAmount();
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource) {
            return 1000;
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return index < size() && (fluidStacks.isEmpty() || resource.matches(fluidStacks.getFirst()) || fluidStacks.getFirst().isEmpty());
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (index >= size()) {
                return 0;
            }
            if (resource.matches(fluidStacks.getFirst())) {
                int insert = Math.min(1000 - getAmountAsInt(index), amount);
                if (resource.matches(fluidStacks.get(index))) {
                    fluidStacks.get(index).grow(insert);
                } else {
                    fluidStacks.set(index, resource.toStack(insert));
                }
                markChanged();
                return insert;
            }
            return 0;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (index >= size()) {
                return 0;
            }
            if (resource.matches(fluidStacks.get(index))) {
                int extract = Math.min(fluidStacks.get(index).getAmount(), amount);
                fluidStacks.get(index).shrink(extract);
                markChanged();
                return extract;
            }
            return 0;
        }
    };

    protected void markChanged() {
        setChanged(getLevel(), getBlockPos(), getBlockState());
    }
}
