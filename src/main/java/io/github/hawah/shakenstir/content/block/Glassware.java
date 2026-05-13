package io.github.hawah.shakenstir.content.block;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.blockEntity.BlockEntityRegistries;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.block.ITakeUpBlock;
import io.github.hawah.shakenstir.util.IModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Glassware extends Block implements ITakeUpBlock, EntityBlock {

    public static final BooleanProperty LONG_DRINK = BooleanProperty.create("long_drink");

    public Glassware(Properties properties) {
        super(properties.noOcclusion().isViewBlocking((_, _, _)->false));
        this.registerDefaultState(this.stateDefinition.any().setValue(LONG_DRINK, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LONG_DRINK);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(LONG_DRINK, false);
    }

    @Override
    public ItemStack getDrop(BlockState state, Level level, BlockPos pos) {
        ItemStack drop = ITakeUpBlock.super.getDrop(state, level, pos);
        if (level.getBlockEntity(pos) instanceof GlasswareBlockEntity blockEntity) {
            drop.set(DataComponentTypeRegistries.DRINK_DATA, blockEntity.contentComponents);
            drop.set(DataComponentTypeRegistries.GLASSWARE_DECORATIONS, blockEntity.decorationsList);
        }
        return drop;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        ItemStack drop = this.asItem().getDefaultInstance();
        if (level.getBlockEntity(pos) instanceof GlasswareBlockEntity blockEntity){
            if (blockEntity.model != null){
                drop.set(DataComponents.ITEM_MODEL, blockEntity.model);
            } else {
                drop.set(DataComponents.ITEM_MODEL, ShakenStir.asResource(drop.is(ItemRegistries.SHORT_DRINK_GLASSWARE)?"martini_glass": "collins_glass"));
            }
            if (blockEntity.pureName != null) {
                drop.set(DataComponents.ITEM_NAME, blockEntity.pureName);
            }
        }
        return drop;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (onUseWithoutItem(state, level, pos, player, hitResult)) {
            return InteractionResult.CONSUME;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    public boolean onUseWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.getMainHandItem().isEmpty() || player.isShiftKeyDown()) {
            return false;
        }
        ITakeUpBlock.holdOrAddItem(player, getDrop(state, level, pos), level, pos);
        level.removeBlock(pos, false);
        return true;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos.below());
        return blockState.isSolidRender() && blockState.isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof GlasswareBlockEntity blockEntity && blockEntity.getLevel().isClientSide()) {
            IModel model = blockEntity.getModel();
            return model.getShape().move(blockEntity.position.x(), 0, blockEntity.position.y());
        }
        return super.getShape(state, level, pos, context);
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
        return directionToNeighbour == Direction.DOWN && !this.canSurvive(state, level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new GlasswareBlockEntity(worldPosition, blockState);
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
    public <T extends BlockEntity> @javax.annotation.Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // You can return different tickers here, depending on whatever factors you want. A common use case would be
        // to return different tickers on the client or server, only tick one side to begin with,
        // or only return a ticker for some blockstates (e.g. when using a "my machine is working" blockstate property).
        if (!level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, BlockEntityRegistries.GLASSWARE_BLOCK_ENTITY.get(), GlasswareBlockEntity::onAnimationTick);
    }

    private static <E extends BlockEntity, A extends BlockEntity> @javax.annotation.Nullable BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> type, BlockEntityType<E> checkedType, BlockEntityTicker<? super E> ticker
    ) {
        return checkedType == type ? (BlockEntityTicker<A>) ticker : null;
    }


}
