package io.github.hawah.shakenstir.content.blockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * TODO
 * 在有酒保方块管理的酒柜中，存放酒瓶后，如果取出
 * 则会从所有管理的酒柜里，找到相同品类的酒以填充原先的格子
 */
public class BartenderBlockEntity extends BlockEntity {
    public BartenderBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    public ResourceHandler<ItemResource> itemHandler = new ResourceHandler<>() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public ItemResource getResource(int index) {
            return null;
        }

        @Override
        public long getAmountAsLong(int index) {
            return 0;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return 0;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return false;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    };
}
