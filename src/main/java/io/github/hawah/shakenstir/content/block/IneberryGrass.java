package io.github.hawah.shakenstir.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class IneberryGrass extends VegetationBlock implements BonemealableBlock, net.neoforged.neoforge.common.IShearable{
    public static final MapCodec<IneberryGrass> CODEC = simpleCodec(IneberryGrass::new);
    private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 13.0);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
    public IneberryGrass(Properties properties) {
        super(properties.noCollision().noOcclusion().isViewBlocking((_,_,_) -> false));
        this.registerDefaultState(defaultBlockState().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
        super.createBlockStateDefinition(builder);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) == 0;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(AGE) == 0) {
            return;
        }
        if (random.nextFloat() < 0.8) {
            return;
        }
        for (int i = 0; i < 5; i++) {
            Vec3 bc = pos.getBottomCenter().add(0, 0.6, 0);
            double v = 0.4;
            bc = bc.add(random.nextFloat() * v - v/2, 0, random.nextFloat() * v - v/2);
            level.addParticle(
                    ParticleTypes.DRIPPING_WATER,
                    bc.x(),
                    bc.y(),
                    bc.z(),
                    0.0,
                    0.0,
                    0.0
            );
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) == 0;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(AGE, 1), 2);
    }
}
