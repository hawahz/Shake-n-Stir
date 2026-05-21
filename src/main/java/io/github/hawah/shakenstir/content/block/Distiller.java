package io.github.hawah.shakenstir.content.block;

import io.github.hawah.shakenstir.foundation.block.DistillerPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jspecify.annotations.Nullable;

public class Distiller extends Block {

    public static final EnumProperty<DistillerPart> PART = EnumProperty.create("part", DistillerPart.class);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public Distiller(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition
                        .any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(PART, DistillerPart.LOWER)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, FACING);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
        Direction facing = state.getValue(FACING);
        level.setBlock(pos.above(), state.setValue(PART, DistillerPart.UPPER), 3);
        level.setBlock(pos.above().relative(facing), state.setValue(PART, DistillerPart.PIPE), 3);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos upperPos = pos.above();
        BlockPos pipePos = upperPos.relative(facing);
        if (pos.getY() < level.getMaxY() - 1
                && level.getBlockState(upperPos).canBeReplaced(context)
                && level.getBlockState(pipePos).canBeReplaced(context)) {
            return this.defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(PART, DistillerPart.LOWER);
        } else {
            return null;
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        DistillerPart part = state.getValue(PART);
        Direction facing = state.getValue(FACING);
        return switch (part) {
            case LOWER -> {
                BlockPos below = pos.below();
                yield level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
            }
            case UPPER -> level.getBlockState(pos.below()).is(this);
            case PIPE -> {
                BlockPos behind = pos.relative(facing.getOpposite());
                yield level.getBlockState(behind).is(this) && level.getBlockState(behind.below()).is(this);
            }
        };
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return mirror == Mirror.NONE ? state : rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected long getSeed(BlockState state, BlockPos pos) {
        DistillerPart part = state.getValue(PART);
        Direction facing = state.getValue(FACING);
        BlockPos root = switch (part) {
            case LOWER -> pos;
            case UPPER -> pos.below();
            case PIPE -> pos.relative(facing.getOpposite()).below();
        };
        return Mth.getSeed(root.getX(), root.getY(), root.getZ());
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
        DistillerPart part = state.getValue(PART);
        Direction facing = state.getValue(FACING);

        if (part == DistillerPart.LOWER && directionToNeighbour == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        boolean isStructuralNeighbor = switch (part) {
            case LOWER -> directionToNeighbour == Direction.UP;
            case UPPER -> directionToNeighbour == Direction.DOWN || directionToNeighbour == facing;
            case PIPE -> directionToNeighbour == facing.getOpposite();
        };

        if (isStructuralNeighbor && !neighbourState.is(this)) {
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }
}