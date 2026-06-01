package io.github.hawah.shakenstir.content.blockEntity;

import io.github.hawah.shakenstir.content.block.Cabinet;
import io.github.hawah.shakenstir.foundation.item.SpiritBottleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;


public class CabinetBlockEntity extends AutoUpdateBlockEntity implements ItemOwner {

    public static final int CABNET_SIZE = 2;

    public final NonNullList<ItemStack> contents = NonNullList.withSize(CABNET_SIZE, ItemStack.EMPTY);

    public CabinetBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityRegistries.CABINET_BLOCK_ENTITY.get(), worldPosition, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        ContainerHelper.saveAllItems(output, contents);
        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        contents.clear();
        ContainerHelper.loadAllItems(input, contents);
        super.loadAdditional(input);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) {
            Containers.dropContents(level, pos, contents);
        }
    }

    public boolean putSpirit(int slot, ItemStack stack) {
        if (!this.contents.get(slot).isEmpty()) {
            return false;
        }
        if (!(stack.getItem() instanceof SpiritBottleItem)) {
            return false;
        }
        this.contents.set(slot, stack.split(1));
        markChanged();
        return true;
    }

    public ItemStack takeSpirit(int slot) {
        if (this.contents.get(slot).isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = this.contents.get(slot);
        this.contents.set(slot, ItemStack.EMPTY);
        markChanged();
        return itemStack;
    }

    public final ResourceHandler<ItemResource> itemHandler = new ResourceHandler<>() {
        @Override
        public int size() {
            return CABNET_SIZE;
        }

        @Override
        public ItemResource getResource(int index) {
            if (index < CABNET_SIZE && index >= 0) {
                return ItemResource.of(contents.get(index));
            }
            return ItemResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index) {
            if (index < CABNET_SIZE && index>=0) {
                return contents.get(index).getCount();
            }
            return 0;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            if (index < CABNET_SIZE && index>=0) {
                return 1;
            }
            return 0;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return index < CABNET_SIZE && index>=0;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index < CABNET_SIZE && index>=0) {
                ItemStack stack = resource.toStack(amount);
                if (contents.get(index).isEmpty()) {
                    contents.set(index, stack);
                    markChanged();
                    return stack.getCount();
                }
            }
            return 0;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index < CABNET_SIZE && index>=0) {
                ItemStack stack = contents.get(index);
                if (!stack.isEmpty()) {
                    int count = Math.min(stack.getCount(), amount);
                    stack.shrink(count);
                    if (stack.isEmpty()) {
                        contents.set(index, ItemStack.EMPTY);
                    }
                    markChanged();
                    return count;
                }
            }
            return 0;
        }
    };

    @Override
    public Level level() {
        return level;
    }

    @Override
    public Vec3 position() {
        return worldPosition.getCenter();
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return this.getBlockState().getValue(Cabinet.FACING).getOpposite().toYRot();
    }
}
