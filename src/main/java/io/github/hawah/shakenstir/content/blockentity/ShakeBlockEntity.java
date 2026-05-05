package io.github.hawah.shakenstir.content.blockentity;

import io.github.hawah.shakenstir.content.block.BlockRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ShakeBlockEntity extends BlockEntity {
    public ShakeBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityRegistries.TELEPHONE_BLOCK_ENTITY.get(), worldPosition, blockState);
    }

    public void reset() {

    }
}
