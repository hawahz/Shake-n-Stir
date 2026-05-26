package io.github.hawah.shakenstir.foundation.item;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class DataTransportableBlockItem extends PriorityBlockItem{
    public DataTransportableBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult interactionResult = super.useOn(context);
        onRemovingData(context);
        return interactionResult;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext placeContext, BlockState placementState) {
        if (!blockCanBePlaced(placeContext)) {
            return false;
        }
        if (super.placeBlock(placeContext, placementState)) {
            onAddingData(placeContext, placementState);
            return true;
        };
        return false;
    }

    public boolean blockCanBePlaced(BlockPlaceContext placeContext) {
        return Direction.UP.equals(placeContext.getClickedFace());
    }

    public abstract void onAddingData(BlockPlaceContext placeContext, BlockState placementState);
    public abstract void onRemovingData(UseOnContext context);
}
