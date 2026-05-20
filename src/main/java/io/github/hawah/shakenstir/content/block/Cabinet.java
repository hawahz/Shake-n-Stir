package io.github.hawah.shakenstir.content.block;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.content.blockEntity.CabinetBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Cabinet extends HorizontalDirectionalBlock implements EntityBlock, SimpleWaterloggedBlock {
    public static final BooleanProperty LEFT = BooleanProperty.create("left");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public Cabinet(Properties properties) {
        super(properties);
        this.registerDefaultState(defaultBlockState()
                .setValue(LEFT, false)
                .setValue(RIGHT, false)
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEFT, RIGHT, FACING, WATERLOGGED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facingDirection = context.getHorizontalDirection().getOpposite();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        FluidState replacedFluidState = level.getFluidState(pos);
        boolean secondaryUse = context.isSecondaryUseActive();
        Direction clickedFace = context.getClickedFace();

        boolean isLeft = false, isRight = false;

        Direction right = facingDirection.getCounterClockWise();
        Direction left = facingDirection.getClockWise();

        BlockState rightBlock = level.getBlockState(pos.relative(right));
        BlockState leftBlock = level.getBlockState(pos.relative(left));

        isLeft = rightBlock.is(this) && rightBlock.getValue(FACING).equals(facingDirection);
        isRight = leftBlock.is(this) && leftBlock.getValue(FACING).equals(facingDirection);
        return this.defaultBlockState().setValue(LEFT, isLeft).setValue(RIGHT, isRight).setValue(WATERLOGGED, replacedFluidState.is(Fluids.WATER));
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction directionToNeighbour,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random
    ) {
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        boolean isLeft = state.getValue(LEFT);
        boolean isRight = state.getValue(RIGHT);

        if (directionToNeighbour.getAxis().isHorizontal()) {
            // left
            if (directionToNeighbour == state.getValue(FACING).getClockWise()) {
                isLeft = this.canConnectTo(neighbourState) && neighbourState.getValue(FACING) == state.getValue(FACING);
            }
            if (directionToNeighbour == state.getValue(FACING).getCounterClockWise()) {
                isRight = this.canConnectTo(neighbourState) && neighbourState.getValue(FACING) == state.getValue(FACING);
            }
        }

        return state.setValue(LEFT, isLeft).setValue(RIGHT, isRight);
    }

    public boolean canConnectTo(BlockState blockState) {
        return blockState.is(this);
    }

    private Direction candidatePartnerFacing(Level level, BlockPos pos, Direction neighbourDirection) {
        BlockState state = level.getBlockState(pos.relative(neighbourDirection));
        return state.getValue(FACING);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return BlockRegistries.CABINET_CODEC.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new CabinetBlockEntity(worldPosition, blockState);
    }
}
