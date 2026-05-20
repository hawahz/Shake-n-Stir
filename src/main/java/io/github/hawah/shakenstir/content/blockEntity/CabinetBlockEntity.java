package io.github.hawah.shakenstir.content.blockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;


public class CabinetBlockEntity extends BlockEntity {

    public static final int CABNET_SIZE = 2;

    public final NonNullList<ItemStack> contents = NonNullList.withSize(CABNET_SIZE, ItemStack.EMPTY);

    public CabinetBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityRegistries.CABINET_BLOCK_ENTITY.get(), worldPosition, blockState);
    }

    public final ResourceHandler<ItemResource> itemHandler = new ResourceHandler<>() {
        @Override
        public int size() {
            return CABNET_SIZE;
        }

        @Override
        public ItemResource getResource(int index) {
            if (index < CABNET_SIZE && index > 0) {
                return ItemResource.of(contents.get(index));
            }
            return ItemResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index) {
            if (index < CABNET_SIZE && index > 0) {
                return contents.get(index).getCount();
            }
            return 0;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            if (index < CABNET_SIZE && index > 0) {
                return 1;
            }
            return 0;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return index < CABNET_SIZE && index > 0;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index < CABNET_SIZE && index > 0) {
                ItemStack stack = resource.toStack(amount);
                if (contents.get(index).isEmpty()) {
                    contents.set(index, stack);
                    return stack.getCount();
                }
            }
            return 0;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index < CABNET_SIZE && index > 0) {
                ItemStack stack = contents.get(index);
                if (!stack.isEmpty()) {
                    int count = Math.min(stack.getCount(), amount);
                    stack.shrink(count);
                    if (stack.isEmpty()) {
                        contents.set(index, ItemStack.EMPTY);
                    }
                    return count;
                }
            }
            return 0;
        }
    };
}
