package io.github.hawah.shakenstir.content.block;

import com.mojang.datafixers.util.Pair;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LemonTreeBlock extends Block implements BonemealableBlock {

    public static final VoxelShape SHAPE = Shapes.or(
            box(5, 0, 5, 11, 14, 11),
            box(-1, 14, -1, 17, 17, 17)
    );
    public static final MapCodec<MintPlantBlock> CODEC = simpleCodec(MintPlantBlock::new);

    public LemonTreeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        List<Pair<BlockPos, BlockState>> list = new ArrayList<>();
        int changeCount = random.nextInt(2);
        if (level.getBlockState(pos.above()).isEmpty()) {
            list.add(Pair.of(pos.above(), BlockRegistries.LEMON_LEAVES.get().defaultBlockState()));
        } else if (level.getBlockState(pos.above(2)).isEmpty()) {
            list.add(Pair.of(pos.above(2), BlockRegistries.LEMON_TOP_LEAVES.get().defaultBlockState()));
        }
        for (Direction direction : HorizontalDirectionalBlock.FACING.getPossibleValues()) {
            if (!level.getBlockState(pos.above().relative(direction)).isEmpty()) {
                continue;
            }
            BlockState leaves = BlockRegistries.LEMON_SIDE_LEAVES.get().defaultBlockState();
            list.add(Pair.of(pos.above().relative(direction), leaves.setValue(LemonSideLeavesBlock.FACING, direction.getOpposite())));
        }
        if (list.isEmpty()) {
            for (Direction direction : HorizontalDirectionalBlock.FACING.getPossibleValues()) {
                BlockState blockState = level.getBlockState(pos.above().relative(direction));
                if (!blockState.isEmpty() && blockState.is(BlockRegistries.LEMON_SIDE_LEAVES) && blockState.getValue(LemonSideLeavesBlock.AGE) < LemonSideLeavesBlock.MAX_AGE) {
                    list.add(Pair.of(pos.above().relative(direction), blockState));
                }
            }
            if (list.isEmpty()) {
                return;
            }
            Collections.shuffle(list);
            BlockState blockState = list.getFirst().getSecond();
            BlockPos blockPos = list.getFirst().getFirst();
            if (blockState.getBlock() instanceof BonemealableBlock block && block.isValidBonemealTarget(level, blockPos, blockState)) {
                if (level instanceof ServerLevel) {
                    if (block.isBonemealSuccess(level, level.getRandom(), blockPos, blockState)) {
                        block.performBonemeal(level, level.getRandom(), blockPos, blockState);
                    }
                }
            }
            return;
        }
        if (list.size() == 1) {
            list.forEach(p -> level.setBlock(p.getFirst(), p.getSecond(), 2));
            return;
        }
        Collections.shuffle(list);
        for (int i = 0; i < changeCount; i++) {
            if (list.isEmpty()) {
                return;
            }
            Pair<BlockPos, BlockState> first = list.getFirst();
            level.setBlock(first.getFirst(), first.getSecond(), 2);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
