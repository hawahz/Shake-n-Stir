package io.github.hawah.shakenstir.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractSpiritBlock extends FallingBlock {
    public AbstractSpiritBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState state, BlockState replacedBlock, FallingBlockEntity entity) {
        if (level.getBlockState(pos.below()).isSolidRender()){
            level.destroyBlock(pos, false);
        }
        super.onLand(level, pos, state, replacedBlock, entity);
    }
}
