package io.github.hawah.shakenstir.content.block;

import io.github.hawah.shakenstir.content.blockEntity.BlockEntityRegistries;
import io.github.hawah.shakenstir.content.blockEntity.DistillerBlockEntity;
import io.github.hawah.shakenstir.foundation.block.DistillerPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class Distiller extends Block implements EntityBlock{

    public static final EnumProperty<DistillerPart> PART = EnumProperty.create("part", DistillerPart.class);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final Map<Direction, VoxelShape> PIPE_SHAPES = Shapes.rotateAll(box(5, 0, 0, 11, 16, 10));

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
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
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
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(PART).equals(DistillerPart.PIPE)) {
            return PIPE_SHAPES.get(state.getValue(FACING).getOpposite());
        }
        return super.getShape(state, level, pos, context);
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

    public static BlockPos findSource(BlockState state, BlockPos pos) {
        DistillerPart part = state.getValue(PART);
        Direction facing = state.getValue(FACING);
        return switch (part) {
            case LOWER -> pos;
            case UPPER -> pos.below();
            case PIPE -> pos.relative(facing.getOpposite()).below();
        };
    }

    @Override
    protected long getSeed(BlockState state, BlockPos pos) {
        BlockPos root = findSource(state, pos);
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

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        if (blockState.getValue(PART).equals(DistillerPart.root())) {
            return new DistillerBlockEntity(worldPosition, blockState);
        }
        return null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // You can return different tickers here, depending on whatever factors you want. A common use case would be
        // to return different tickers on the client or server, only tick one side to begin with,
        // or only return a ticker for some blockstates (e.g. when using a "my machine is working" blockstate property).
        if (!state.getValue(PART).equals(DistillerPart.root())) {
            return null;
        }
        return createTickerHelper(type, BlockEntityRegistries.DISTILLER_BLOCK_ENTITY.get(), DistillerBlockEntity::tick);
    }

    @SuppressWarnings("unchecked")
    private static <E extends BlockEntity, A extends BlockEntity> @Nullable BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> type, BlockEntityType<E> checkedType, BlockEntityTicker<? super E> ticker
    ) {
        return checkedType == type ? (BlockEntityTicker<A>) ticker : null;
    }
}