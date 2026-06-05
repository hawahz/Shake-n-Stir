package io.github.hawah.shakenstir.content.block;

import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.content.blockEntity.BarMenuBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 存储放下该方块的实体
 * 当有玩家下单后，提醒该实体
 */
public class BarMenuBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(box(3.5, 0, 1, 12.5, 1, 15));

    public BarMenuBlock(Properties properties) {
        super(properties.noOcclusion().noCollision());
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return BlockRegistries.BAR_MENU_CODEC.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BarMenuBlockEntity barMenuBlockEntity) {
            if (!level.isClientSide() && player.preventsBlockDrops()) {
                ItemStack itemStack = new ItemStack(state.getBlock());
                barMenuBlockEntity.recipes.forEach(recipe -> recipe.right().count = 0);
                if (!barMenuBlockEntity.recipes.isEmpty()){
                    itemStack.set(DataComponentTypeRegistries.RECIPES_DATA, new ArrayList<>(barMenuBlockEntity.recipes));
                }
                if (barMenuBlockEntity.bkg != null) {
                    itemStack.set(DataComponentTypeRegistries.MENU_BKG, barMenuBlockEntity.bkg);
                }
                if (itemStack.isComponentsPatchEmpty()) {
                    return super.playerWillDestroy(level, pos, state, player);
                }
                ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemStack);
                entity.setDefaultPickUpDelay();
                level.addFreshEntity(entity);
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection();
        return this.defaultBlockState()
                .setValue(FACING, context.isSecondaryUseActive()? direction.getOpposite(): direction)
                ;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new BarMenuBlockEntity(worldPosition, blockState);
    }

    public static final Identifier CONTENTS = Identifier.withDefaultNamespace("contents");
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        List<ItemStack> drop = super.getDrops(state, params);
        if (blockEntity instanceof BarMenuBlockEntity bar) {
            drop = drop.stream().peek(itemStack -> {
                bar.recipes.forEach(recipe -> recipe.right().count = 0);
                itemStack.set(DataComponentTypeRegistries.RECIPES_DATA, new ArrayList<>(bar.recipes));
                if (bar.bkg != null) {
                    itemStack.set(DataComponentTypeRegistries.MENU_BKG, bar.bkg);
                }
            }).toList();
        }
        return drop;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof BarMenuBlockEntity blockEntity) {
            UUID placerId = blockEntity.getPlacerId();
            Entity entity = level.getEntity(placerId);
            player.swing(InteractionHand.MAIN_HAND);
            if (entity instanceof Player playerPlacer) {
                playerPlacer.sendOverlayMessage(Component.literal("alert"));
            } else if (entity instanceof BartenderEntity bartender) {
                bartender.alertCustomerOrdered(player);
            }
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}
