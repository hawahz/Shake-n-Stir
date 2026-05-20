package io.github.hawah.shakenstir.content.block;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.content.blockEntity.CabinetBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Cabinet extends HorizontalDirectionalBlock implements EntityBlock, SimpleWaterloggedBlock {
    public static final BooleanProperty LEFT = BooleanProperty.create("left");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public Cabinet(Properties properties) {
        super(properties.noOcclusion());
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
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(LEFT, isLeft)
                .setValue(RIGHT, isRight)
                .setValue(WATERLOGGED, replacedFluidState.is(Fluids.WATER));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        Direction facing = state.getValue(FACING);
        if (hitResult.getDirection().getOpposite() != facing || itemStack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        int index = getSlot(pos, hitResult, facing);
        if (level.getBlockEntity(pos) instanceof CabinetBlockEntity blockEntity) {
            if (blockEntity.putSpirit(index, player.isCreative()? itemStack.copy(): itemStack)) {
                return InteractionResult.SUCCESS;
            }
        }

        return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
    }

    public static int getSlot(BlockPos pos, BlockHitResult hitResult, Direction facing) {
        Vec3 leftDir = facing.getClockWise().getUnitVec3();
        Vec3 leftCenter = pos.getCenter().add(leftDir.x * 0.5, 0, leftDir.z * 0.5);
        Vec3 rightCenter = pos.getCenter().add(-leftDir.x * 0.5, 0, -leftDir.z * 0.5);
        Vec3 location = hitResult.getLocation();
        int index;
        if (location.distanceTo(leftCenter) < location.distanceTo(rightCenter)) {
            index = 0;
        } else {
            index = 1;
        }
        return index;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        Direction facing = state.getValue(FACING);
        if (hitResult.getDirection().getOpposite() != facing) {
            return InteractionResult.FAIL;
        }
        int index = getSlot(pos, hitResult, facing);
        if (level.getBlockEntity(pos) instanceof CabinetBlockEntity blockEntity) {
            ItemStack itemStack = blockEntity.takeSpirit(index);
            if (!itemStack.isEmpty()) {
                player.addItem(itemStack);
                return InteractionResult.SUCCESS;
            }
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        return super.getCloneItemStack(level, pos, state, includeData, player);
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
