package io.github.hawah.shakenstir.content.blockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DistillerBlockEntity extends BlockEntity {
    public DistillerBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityRegistries.DISTILLER_BLOCK_ENTITY.get(), worldPosition, blockState);
    }

    public static void tick(Level ignoredLevel, BlockPos ignoredPos, BlockState ignoredState, DistillerBlockEntity blockEntity) {
    }
}
