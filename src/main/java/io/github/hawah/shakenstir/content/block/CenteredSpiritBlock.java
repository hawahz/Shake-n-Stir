package io.github.hawah.shakenstir.content.block;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.FluidStackDataComponent;
import io.github.hawah.shakenstir.content.recipe.Spirits;
import io.github.hawah.shakenstir.foundation.block.AbstractSpiritBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CenteredSpiritBlock extends AbstractSpiritBlock {

    public static final VoxelShape SHAPE = box(5, 0 ,5, 11, 15, 11);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public CenteredSpiritBlock(Properties properties) {
        super(properties.sound(SoundType.GLASS));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(VOLUME, 4).setValue(CONTENT, Spirits.GIN));
    }

    public CenteredSpiritBlock(Properties properties, Spirits spirits) {
        super(properties.sound(SoundType.GLASS));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(VOLUME, 4).setValue(CONTENT, spirits));
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
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING).add(VOLUME).add(CONTENT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidStackDataComponent fluidStackDataComponent = context.getItemInHand().getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, FluidStackDataComponent.EMPTY);
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(VOLUME, fluidStackDataComponent.fluidStack().getAmount()/250)
                .setValue(CONTENT, Spirits.fromFluid(fluidStackDataComponent.fluidStack().getFluidType()))
                ;
    }

    @Override
    public ItemStack getDrop(BlockState state, Level level, BlockPos pos) {
        ItemStack drop = super.getDrop(state, level, pos);
        FluidStack stack = new FluidStack(state.getValueOrElse(CONTENT, Spirits.GIN).getFluid(), state.getValueOrElse(VOLUME, 0) * 250);
        drop.set(DataComponentTypeRegistries.SPIRIT_CONTENT, new FluidStackDataComponent(stack));
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
}
