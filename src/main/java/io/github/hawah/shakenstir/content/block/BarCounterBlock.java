package io.github.hawah.shakenstir.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jspecify.annotations.Nullable;

public class BarCounterBlock extends HorizontalDirectionalBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;


    public BarCounterBlock(Properties properties) {
        super(properties.sound(SoundType.WOOD).strength(2.0F, 3.0F));
        this.registerDefaultState(this.defaultBlockState()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return BlockRegistries.BAR_COUNTER_CODEC.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH, EAST, SOUTH, WEST, FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos clickedPos = context.getClickedPos();
        Level level = context.getLevel();
        Direction clickedFace = context.getClickedFace();
        BlockState clickBlock = level.getBlockState(clickedPos.relative(clickedFace.getOpposite()));
        Direction facing = context.getHorizontalDirection();
        if (clickBlock.is(this)) {
            facing = clickBlock.getValue(FACING);
        }
        boolean north = level.getBlockState(clickedPos.north()).is(this);
        boolean east = level.getBlockState(clickedPos.east()).is(this);
        boolean south = level.getBlockState(clickedPos.south()).is(this);
        boolean west = level.getBlockState(clickedPos.west()).is(this);
        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(NORTH, north)
                .setValue(EAST, east)
                .setValue(SOUTH, south)
                .setValue(WEST, west)
                ;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        boolean north = state.getValue(NORTH);
        boolean east =  state.getValue(EAST);
        boolean south = state.getValue(SOUTH);
        boolean west =  state.getValue(WEST);

        switch (directionToNeighbour) {
            case NORTH -> north = neighbourState.is(this);
            case EAST -> east = neighbourState.is(this);
            case SOUTH -> south = neighbourState.is(this);
            case WEST -> west = neighbourState.is(this);
        }
        return state
                .setValue(NORTH, north)
                .setValue(EAST, east)
                .setValue(SOUTH, south)
                .setValue(WEST, west)
                ;
    }
}
