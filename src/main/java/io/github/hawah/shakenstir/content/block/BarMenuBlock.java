package io.github.hawah.shakenstir.content.block;

import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.content.blockEntity.BarMenuBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 存储放下该方块的实体
 * 当有玩家下单后，提醒该实体
 */
public class BarMenuBlock extends BaseEntityBlock {
    public BarMenuBlock(Properties properties) {
        super(properties.noOcclusion().noCollision());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return BlockRegistries.BAR_MENU_CODEC.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new BarMenuBlockEntity(worldPosition, blockState);
    }
}
