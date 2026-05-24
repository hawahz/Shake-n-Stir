package io.github.hawah.shakenstir.content.block;

import io.github.hawah.shakenstir.content.blockEntity.BlockEntityRegistries;
import io.github.hawah.shakenstir.content.blockEntity.DistillerBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.SpiritContent;
import io.github.hawah.shakenstir.foundation.block.DistillerPart;
import io.github.hawah.shakenstir.foundation.block.ITakeUpBlock;
import io.github.hawah.shakenstir.foundation.item.SpiritBottleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
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
        if (!state.getValue(PART).equals(DistillerPart.LOWER)) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof DistillerBlockEntity be) || be.getBurnTicks() <= 0) {
            return;
        }
        double x = pos.getX() + 0.5;
        double y = pos.getY();
        double z = pos.getZ() + 0.5;
        if (random.nextDouble() < 0.1) {
            level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }

        Direction direction = state.getValue(FACING);
        Direction.Axis axis = direction.getAxis();
        double r = 0.52;
        double ss = random.nextDouble() * 0.6 - 0.3;
        double dx = axis == Direction.Axis.X ? direction.getStepX() * 0.52 : ss;
        double dy = random.nextDouble() * 6.0 / 16.0;
        double dz = axis == Direction.Axis.Z ? direction.getStepZ() * 0.52 : ss;
        level.addParticle(ParticleTypes.SMOKE, x + dx, y + dy, z + dz, 0.0, 0.0, 0.0);
        level.addParticle(ParticleTypes.FLAME, x + dx, y + dy, z + dz, 0.0, 0.0, 0.0);
        if (random.nextInt(10) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    SoundEvents.CAMPFIRE_CRACKLE,
                    SoundSource.BLOCKS,
                    0.5F + random.nextFloat(),
                    random.nextFloat() * 0.7F + 0.6F,
                    false
            );
        }
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
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(PART).equals(DistillerPart.PIPE) && context instanceof EntityCollisionContext) {
            return Shapes.empty();
        }
        return super.getCollisionShape(state, level, pos, context);
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
            case LOWER -> true;
            case UPPER -> level.getBlockState(pos.below()).is(this);
            case PIPE -> {
                BlockPos behind = pos.relative(facing.getOpposite());
                yield level.getBlockState(behind).is(this) && level.getBlockState(behind.below()).is(this);
            }
        };
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (!(level.getBlockEntity(findSource(state, pos)) instanceof DistillerBlockEntity be)) {
            return 0;
        }
        return switch (state.getValue(PART)) {
            case LOWER -> be.getBurnTicks() * 15 / DistillerBlockEntity.FUEL_MAX;
            case UPPER -> be.getInputFluid().amount() * 15 / DistillerBlockEntity.MAX_INPUT_FLUID_CAPACITY;
            case PIPE -> {
                if (be.getProduct().isEmpty()) {
                    yield 0;
                }
                yield be.getProduct().amount() * 15 / DistillerBlockEntity.MAX_PRODUCT_FLUID_CAPACITY;
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
        if (!state.getValue(PART).equals(DistillerPart.root())) {
            return null;
        }
        if (level.isClientSide()) {
            return createTickerHelper(type, BlockEntityRegistries.DISTILLER_BLOCK_ENTITY.get(), DistillerBlockEntity::animationTick);
        }
        return createTickerHelper(type, BlockEntityRegistries.DISTILLER_BLOCK_ENTITY.get(), DistillerBlockEntity::serverTick);
    }

    @SuppressWarnings("unchecked")
    private static <E extends BlockEntity, A extends BlockEntity> @Nullable BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> type, BlockEntityType<E> checkedType, BlockEntityTicker<? super E> ticker
    ) {
        return checkedType == type ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockPos rootPos = findSource(state, pos);
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!(level.getBlockEntity(rootPos) instanceof DistillerBlockEntity be)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }


        DistillerPart part = state.getValue(PART);

        if (part == DistillerPart.UPPER) {
            ResourceHandler<FluidResource> handler = stack.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forStack(stack));
            if (handler != null && handler.size() > 0) {
                FluidResource resource = handler.getResource(0);
                int amount = handler.getAmountAsInt(0);
                if (amount > 0) {
                    FluidStack fluidStack = resource.toStack(amount);
                    if (be.insertFluid(fluidStack, player.isCreative())) {
                        int inserted = amount - fluidStack.getAmount();
                        if (!player.isCreative() && inserted > 0) {
                            try (Transaction tx = Transaction.openRoot()) {
                                handler.extract(0, resource, inserted, tx);
                            }
                        }
                        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                        player.getCooldowns().addCooldown(stack, 10);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            if (!stack.isEmpty()) {
                be.insertItem(stack, player);
                return InteractionResult.SUCCESS;
            }
        }

        if (part == DistillerPart.LOWER) {
            if (be.insertFuel(stack, player)) {
                return InteractionResult.SUCCESS;
            }
        }

        if (part == DistillerPart.PIPE) {
            if (stack.getItem() instanceof SpiritBottleItem && !be.getProduct().isEmpty()) {
                SpiritContent spiritContent;
                if ((spiritContent = stack.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, SpiritContent.EMPTY)).isEmpty() || spiritContent.fluidStack().is(be.getProduct().getFluid())) {
                    int extract = 0;
                    FluidResource fluidResource = FluidResource.of(be.getProduct());
                    try (Transaction tx = Transaction.openRoot()){
                         extract = be.getProductHandler().extract(
                                0,
                                be.getProductHandler().getResource(0),
                                1000 - spiritContent.fluidStack().amount(),
                                tx);

                    }
                    if (extract > 0) {
                        SpiritContent content = new SpiritContent(fluidResource.toStack(spiritContent.fluidStack().amount() + extract));
                        stack.set(DataComponentTypeRegistries.SPIRIT_CONTENT, content);
                        player.swing(hand);
                        player.playSound(
                                SoundEvents.BOTTLE_FILL,
                                1.0F,
                                1.0F
                        );
                    }
                }
            }
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockPos rootPos = findSource(state, pos);
        if (!(level.getBlockEntity(rootPos) instanceof DistillerBlockEntity be)) {
            return InteractionResult.PASS;
        }

        if (state.getValue(PART) == DistillerPart.UPPER) {
            ItemStack popped = be.popItem();
            if (!popped.isEmpty()) {
                ITakeUpBlock.holdOrAddItem(player, popped, level, pos);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }
}