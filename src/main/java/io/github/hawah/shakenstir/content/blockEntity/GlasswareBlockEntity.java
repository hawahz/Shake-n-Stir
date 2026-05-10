package io.github.hawah.shakenstir.content.blockEntity;

import com.mojang.logging.LogUtils;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.util.SerializeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.joml.Vector2f;

public class GlasswareBlockEntity extends BlockEntity {

    // To Save
    public final Vector2f position = new Vector2f();
    public float rotation;
    // End Save

    public GlasswareBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityRegistries.GLASSWARE_BLOCK_ENTITY.get(), worldPosition, blockState);
    }

    public int getColor() {
        return 0;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        SerializeHelper.loadVector2f(input, position);
        rotation = input.getFloatOr("Rot", 0.0f);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag var4;
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), LogUtils.getLogger())) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            var4 = output.buildResult();
        }
        return var4;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        SerializeHelper.saveVector2f(output, position);
        output.putFloat("Rot", rotation);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        position.set(components.getOrDefault(DataComponentTypeRegistries.GLASSWARE_POSITION, new Vector2f()));
        rotation = components.getOrDefault(DataComponentTypeRegistries.GLASSWARE_ROTATION, 0.0f);
    }
}
