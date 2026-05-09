package io.github.hawah.shakenstir.content.block;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.content.blockEntity.SpiritBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.FluidStackDataComponent;
import io.github.hawah.shakenstir.foundation.block.ITakeUpBlock;
import io.github.hawah.shakenstir.lib.VoxelShapeMaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SpiritBlock extends FallingBlock implements EntityBlock, ITakeUpBlock{

    public static final VoxelShape SHAPE = box(5, 0 ,5, 11, 15, 11);
    public static final Map<Direction, VoxelShape> DOUBLE_SHAPES = Map.of(
            Direction.NORTH, VoxelShapeMaker.getByHorizontalDirection(Direction.NORTH,  box(0, 0 ,5, 16, 15, 11)),
            Direction.EAST, VoxelShapeMaker.getByHorizontalDirection(Direction.EAST,    box(0, 0 ,5, 16, 15, 11)),
            Direction.SOUTH, VoxelShapeMaker.getByHorizontalDirection(Direction.SOUTH,  box(0, 0 ,5, 16, 15, 11)),
            Direction.WEST, VoxelShapeMaker.getByHorizontalDirection(Direction.WEST,    box(0, 0 ,5, 16, 15, 11))
    );
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty COUNTS = IntegerProperty.create("shakenstir_counts", 1, 4);

    public SpiritBlock(Properties properties) {
        super(properties.sound(SoundType.GLASS).noOcclusion().isViewBlocking((_, _, _)->false));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(COUNTS, 1));
    }

    private static boolean canReplace(ItemStack itemStack, BlockState state) {
        return itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock().equals(state.getBlock()) && state.getValue(COUNTS) < 4;
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return BlockRegistries.CENTERED_SPIRIT_CODEC.get();
    }

    @Override
    public int getDustColor(BlockState blockState, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(COUNTS) == 1) {
            return SHAPE;
        }
        if (state.getValue(COUNTS) == 2) {
            return DOUBLE_SHAPES.get(state.getValue(FACING));
        }
        return Shapes.block();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING).add(COUNTS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        BlockState state = context.getLevel().getBlockState(pos);
        Level level = context.getLevel();
        ItemStack itemStack = context.getItemInHand();
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        int original = state.getValueOrElse(COUNTS, 0);
        if (state.is(this) && original < 4) {
            if (level.getBlockEntity(pos) instanceof SpiritBlockEntity blockEntity) {
                blockEntity.pushAnother(original, itemStack, true);
            }
            return state.setValue(COUNTS, original + 1);
        }
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public ItemStack getDrop(BlockState state, Level level, BlockPos pos) {
        ItemStack drop = ITakeUpBlock.super.getDrop(state, level, pos);
        if (level.getBlockEntity(pos) instanceof SpiritBlockEntity spiritBlockEntity) {
            FluidStack stack = spiritBlockEntity.getFluidStacks().get(state.getValue(COUNTS) - 1);
            drop.set(DataComponentTypeRegistries.SPIRIT_CONTENT, new FluidStackDataComponent(stack.copy()));
        }
        return drop;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new SpiritBlockEntity(worldPosition, blockState);
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

    public boolean onUseWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.isShiftKeyDown() || !player.getMainHandItem().isEmpty()) {
            return false;
        }
        ITakeUpBlock.holdOrAddItem(player, getDrop(state, level, pos), level, pos);
        if (state.getValue(COUNTS) == 1) {
            level.removeBlock(pos, false);
        } else {
            level.setBlockAndUpdate(pos, state.setValue(COUNTS, state.getValue(COUNTS) - 1));
        }
        return true;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (state.getValue(COUNTS) == 4 || player.isShiftKeyDown()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (canReplace(itemStack, state)) {
            int original = state.getValue(COUNTS);
            level.setBlockAndUpdate(pos, state.setValue(COUNTS, original + 1));
            if (level.getBlockEntity(pos) instanceof SpiritBlockEntity blockEntity) {
                blockEntity.pushAnother(original, itemStack, player.isCreative());
            }
            player.swing(hand);
            return InteractionResult.CONSUME;
        }
        return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        ItemStack itemStack = context.getItemInHand();
        return (super.canBeReplaced(state, context) || canReplace(itemStack, state)) && !Optional.ofNullable(context.getPlayer()).map(Player::isShiftKeyDown).orElse(false);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos.below());
        return blockState.isFaceSturdy(level, pos, Direction.UP) || blockState.isEmpty() || blockState.getBlock() instanceof SpiritBlock;
    }
}
