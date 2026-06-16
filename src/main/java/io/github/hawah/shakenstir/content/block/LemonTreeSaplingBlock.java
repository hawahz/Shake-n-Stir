package io.github.hawah.shakenstir.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LemonTreeSaplingBlock extends VegetationBlock implements BonemealableBlock {
    public static final MapCodec<LemonTreeSaplingBlock> CODEC = Block.simpleCodec(LemonTreeSaplingBlock::new);
    public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
    private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 12.0);

    @Override
    public MapCodec<? extends LemonTreeSaplingBlock> codec() {
        return CODEC;
    }

    public LemonTreeSaplingBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
            this.advanceTree(level, pos, state, random);
        }
    }

    public void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource ignoredRandom) {
        if (state.getValue(STAGE) == 0) {
            level.setBlock(pos, state.cycle(STAGE), 260);
        } else if (canGrow(level, pos)) {
            level.setBlock(pos.above(), BlockRegistries.LEMON_LEAVES.get().defaultBlockState(), 2);
            level.setBlock(pos.above(2), BlockRegistries.LEMON_TOP_LEAVES.get().defaultBlockState(), 2);
            for (Direction direction : HorizontalDirectionalBlock.FACING.getPossibleValues()) {
                BlockState leaves = BlockRegistries.LEMON_SIDE_LEAVES.get().defaultBlockState();
                level.setBlock(pos.above().relative(direction), leaves.setValue(LemonSideLeavesBlock.FACING, direction.getOpposite()), 2);
            }
            level.setBlock(pos, BlockRegistries.LEMON_LOG.get().defaultBlockState(), 2);
        }
    }

    public boolean canGrow(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos.above()).isEmpty()) {
            return false;
        }
        if (!level.getBlockState(pos.above(2)).isEmpty()) {
            return false;
        }
        for (Direction direction : HorizontalDirectionalBlock.FACING.getPossibleValues()) {
            if (!level.getBlockState(pos.above().relative(direction)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return level.getRandom().nextFloat() < 0.45;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        this.advanceTree(level, pos, state, random);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }
}
