package io.github.hawah.shakenstir.foundation.block;

import io.github.hawah.shakenstir.content.recipe.Spirits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public abstract class AbstractSpiritBlock extends FallingBlock implements ITakeUpBlock {
    public static final IntegerProperty VOLUME = IntegerProperty.create("spirit_volume", 0, 4);
    public static final EnumProperty<Spirits> CONTENT = EnumProperty.create("spirit_content", Spirits.class);

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

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (onUseWithoutItem(state, level, pos, player, hitResult)) {
            return InteractionResult.CONSUME;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos.below());
        return blockState.isFaceSturdy(level, pos, Direction.UP) || blockState.isEmpty() || blockState.getBlock() instanceof AbstractSpiritBlock;
    }
}
