package io.github.hawah.shakenstir.content.block;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.content.blockEntity.BlockEntityRegistries;
import io.github.hawah.shakenstir.content.blockEntity.ShakeBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.FluidStackDataComponent;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.block.ITakeUpBlock;
import io.github.hawah.shakenstir.lib.VoxelShapeMaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Shake extends FallingBlock implements EntityBlock, ITakeUpBlock {


    public static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.UP, box(5, 0, 5, 11, 11, 11),
            Direction.DOWN, box(5, 0, 5, 11, 8, 11),
            Direction.SOUTH, VoxelShapeMaker.getByHorizontalDirection(Direction.SOUTH, box(5, 0, 5, 11, 5, 13)),
            Direction.NORTH, VoxelShapeMaker.getByHorizontalDirection(Direction.NORTH, box(5, 0, 5, 11, 5, 13)),
            Direction.EAST, VoxelShapeMaker.getByHorizontalDirection(Direction.EAST, box(5, 0, 5, 11, 5, 13)),
            Direction.WEST, VoxelShapeMaker.getByHorizontalDirection(Direction.WEST, box(5, 0, 5, 11, 5, 13))
    );

    public static final EnumProperty<Direction> FACING = DirectionalBlock.FACING;

    public Shake(Properties properties) {
        super(properties.sound(SoundType.METAL).pushReaction(PushReaction.PUSH_ONLY));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return BlockRegistries.SHAKE_CODEC.get();
    }

    @Override
    public int getDustColor(BlockState blockState, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ShakeBlockEntity(blockPos, blockState);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected void falling(FallingBlockEntity entity) {
        super.falling(entity);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        ItemStack itemInHand = context.getItemInHand();
        return this.defaultBlockState().setValue(FACING, itemInHand.getOrDefault(DataComponentTypeRegistries.HAS_CUP, true)? Direction.UP: Direction.DOWN);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack toolStack, boolean willHarvest, FluidState fluid) {
        if (!player.isCreative()) {
            ITakeUpBlock.holdOrAddItem(player, getDrop(state, level, pos), level, pos);
        }
        return super.onDestroyedByPlayer(state, level, pos, player, toolStack, willHarvest, fluid);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        Direction facing = oldState.getValueOrElse(FACING, Direction.UP);
        if (movedByPiston && facing.getAxis().isHorizontal() && !state.getValueOrElse(FACING, Direction.DOWN).getAxis().isHorizontal()) {
            overturn(state, level, pos, facing.getOpposite());
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState state, BlockState replacedBlock, FallingBlockEntity entity) {
        Direction direction = Util.getRandom(Direction.Plane.HORIZONTAL.stream().toList(), level.getRandom());
        overturn(state, level, pos, direction);
        super.onLand(level, pos, state, replacedBlock, entity);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (state.getValue(FACING).getAxis().isHorizontal() || !(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        Vec3 vectorDir = livingEntity.getKnownSpeed().multiply(1, 0, 1);
        double speed = vectorDir.length();
        if (speed < 0.21) {
            return;
        }
        boolean isKnocked = speed > 0.26 || (level.getRandom().nextDouble() < (speed - 0.21)*2);
        if (isKnocked) {
            overturn(state, level, pos, livingEntity.getDirection().getOpposite());
        }
    }

    public void overturn(BlockState state,
                         Level level,
                         BlockPos pos,
                         Direction direction) {
        level.playSound(
                null,
                pos,
                SoundEvents.METAL_STEP,
                SoundSource.BLOCKS,
                1,
                1
        );
        if (state.getValue(FACING).getAxis().isHorizontal()) {
            return;
        }

        Vec3 relative = pos.relative(direction.getOpposite()).getCenter().add(pos.getCenter()).multiply(0.5, 0.5, 0.5);
        if (state.getValue(FACING).equals(Direction.UP)){
            ItemEntity cupItem = new ItemEntity(level, relative.x(), Math.floor(relative.y()), relative.z(), ItemRegistries.SHAKE_CUP.get().getDefaultInstance());
            cupItem.setDeltaMovement(direction.getUnitVec3().multiply(-0.35, 0, -0.35));
            cupItem.setPickUpDelay(20);
            level.addFreshEntity(cupItem);
        }
        level.playSound(
                null,
                pos,
                SoundEvents.BUCKET_EMPTY,
                SoundSource.BLOCKS,
                1,
                1
        );
        ParticleUtils.spawnParticleInBlock(
                level,
                BlockPos.containing(relative),
                10,
                ParticleTypes.FALLING_WATER
        );
        level.setBlockAndUpdate(pos, state.setValue(FACING, direction));
        if (level.getBlockEntity(pos) instanceof ShakeBlockEntity blockEntity) {
            blockEntity.reset();
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (onUseWithoutItem(state, level, pos, player, hitResult)) {
            return InteractionResult.SUCCESS;
        }
        if (state.getValue(FACING).equals(Direction.UP)) {
            level.setBlockAndUpdate(pos, state.setValue(FACING, Direction.DOWN));
            level.playSound(
                    null,
                    pos,
                    SoundEvents.METAL_STEP,
                    SoundSource.BLOCKS,
                    1,
                    1
            );
            ITakeUpBlock.holdOrAddItem(player, ItemRegistries.SHAKE_CUP.toStack(), level, pos);
            return InteractionResult.SUCCESS;
        }

        if (state.getValue(FACING).equals(Direction.DOWN) && level.getBlockEntity(pos) instanceof ShakeBlockEntity blockEntity) {
            ItemStack itemStack = blockEntity.popItem();
            if (!itemStack.isEmpty()) {
                ITakeUpBlock.holdOrAddItem(player, itemStack, level, pos);
                return InteractionResult.SUCCESS;
            }
        }

        if (state.getValue(FACING).getAxis().isHorizontal()) {
            level.setBlockAndUpdate(pos, state.setValue(FACING, Direction.DOWN));
            level.playSound(
                    null,
                    pos,
                    SoundEvents.METAL_STEP,
                    SoundSource.BLOCKS,
                    1,
                    1
            );
            return InteractionResult.SUCCESS;
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    public static boolean canInsert(ItemStack itemStack) {
        if (itemStack.tags().anyMatch(Tags.Items.FOODS_FRUIT::equals)) {
            return true;
        }
        if (itemStack.is(Items.SUGAR)) {
            return true;
        }
        return false;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (itemStack.is(ItemRegistries.SHAKE_CUP)) {
            return tryPlaceCup(itemStack, state, level, pos, player);
        }
        FluidStackDataComponent fluidHolder;
        if (!player.getCooldowns().isOnCooldown(itemStack) && !(fluidHolder = itemStack.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, FluidStackDataComponent.EMPTY)).isEmpty()) {
            return tryPourLiquid(state, level, pos, player, fluidHolder, itemStack, hitResult);
        }
        if (state.getValue(FACING).equals(Direction.DOWN) && !itemStack.isEmpty() && canInsert(itemStack)) {
            if (level.getBlockEntity(pos) instanceof ShakeBlockEntity blockEntity) {
                blockEntity.putItem(itemStack, player.isCreative());
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.TryEmptyHandInteraction.TRY_WITH_EMPTY_HAND;
    }

    private static InteractionResult tryPourLiquid(BlockState state, Level level, BlockPos pos, Player player, FluidStackDataComponent fluidHolder, ItemStack itemStack, BlockHitResult hitResult) {
        if (state.getValue(FACING).equals(Direction.UP) || player.isShiftKeyDown())
            return InteractionResult.TryEmptyHandInteraction.TRY_WITH_EMPTY_HAND;
        FluidStack fluidStack = fluidHolder.fluidStack().copy();
        boolean success = false;
        if (level.getBlockEntity(pos, BlockEntityRegistries.SHAKE_BLOCK_ENTITY.get()).get() instanceof ShakeBlockEntity shakeBlockEntity) {
            success = shakeBlockEntity.pourLiquid(fluidStack, player.isCreative());

        }

        if (!success) {
            return InteractionResult.TryEmptyHandInteraction.TRY_WITH_EMPTY_HAND;
        }

        if (itemStack.getCount() == 1) {
            itemStack.remove(DataComponentTypeRegistries.SPIRIT_CONTENT);
            if (!fluidStack.isEmpty()) {
                itemStack.set(DataComponentTypeRegistries.SPIRIT_CONTENT, new FluidStackDataComponent(fluidStack));
            }
        }
        if ((!player.isCreative() || true) && itemStack.getCount() > 1) {
            itemStack = itemStack.consumeAndReturn(1, player);
            itemStack.remove(DataComponentTypeRegistries.SPIRIT_CONTENT);
            if (!fluidStack.isEmpty()){
                itemStack.set(DataComponentTypeRegistries.SPIRIT_CONTENT, new FluidStackDataComponent(fluidStack));
            }
            ITakeUpBlock.holdOrAddItem(player, itemStack, level, pos);
        }

        level.playSound(
                null,
                pos,
                SoundEvents.WATER_AMBIENT,
                SoundSource.BLOCKS,
                1,
                1
        );
        player.getCooldowns().addCooldown(itemStack, 10);

        return InteractionResult.SUCCESS;
    }

    private static InteractionResult tryPlaceCup(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player) {
        if (!itemStack.is(ItemRegistries.SHAKE_CUP) || state.getValue(FACING).equals(Direction.UP) || player.isShiftKeyDown())
            return InteractionResult.TryEmptyHandInteraction.TRY_WITH_EMPTY_HAND;
        if (!player.isCreative()) {
            itemStack.shrink(1);
        }
        level.setBlockAndUpdate(pos, state.setValue(FACING, Direction.UP));
        level.playSound(
                null,
                pos,
                SoundEvents.METAL_STEP,
                SoundSource.BLOCKS,
                1,
                1
        );
        return InteractionResult.SUCCESS;
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
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos.below());
        return blockState.isFaceSturdy(level, pos, Direction.UP) || blockState.isEmpty();
    }

    @Override
    public ItemStack getDrop(BlockState state, Level level, BlockPos pos) {
        ItemStack stack = ItemRegistries.SHAKE.toStack();
        stack.set(DataComponentTypeRegistries.HAS_CUP, state.getValue(FACING).equals(Direction.UP));
        if (level.getBlockEntity(pos) instanceof ShakeBlockEntity blockEntity) {
            stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(blockEntity.getItemToRender()));
        }
        return stack;
    }
}
