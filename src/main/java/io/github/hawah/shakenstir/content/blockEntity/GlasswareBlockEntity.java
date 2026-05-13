package io.github.hawah.shakenstir.content.blockEntity;

import com.mojang.logging.LogUtils;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.util.IModel;
import io.github.hawah.shakenstir.util.Models;
import io.github.hawah.shakenstir.util.SerializeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.joml.Vector2f;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GlasswareBlockEntity extends BlockEntity {

    // To Save
    public final Vector2f position = new Vector2f();
    public float rotation;
    public Identifier model = null;
    public Component pureName = null;
    public PatchedDataComponentMap contentComponents = new PatchedDataComponentMap(DataComponentMap.EMPTY);

    public float heightRate = 0;
    // End Save

    // Client Animation
    public final Vector2f oPosition = new Vector2f();
    public float oRotation = 0;
    public float oHeight = 0;
    public float height = 0;
    // End

    public GlasswareBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityRegistries.GLASSWARE_BLOCK_ENTITY.get(), worldPosition, blockState);
    }

    public int getColor() {
        return 0;
    }

    public boolean pourProduct(ItemStack itemStack) {
        if (!this.components().isEmpty()) {
            return false;
        }
        contentComponents.setAll(itemStack.getComponents());
        heightRate = 1.0F;
        this.setChanged();
        return true;
    }

    public static void onAnimationTick(Level level, BlockPos pos, BlockState state, GlasswareBlockEntity blockEntity) {
        blockEntity.oHeight = blockEntity.height;
        blockEntity.height = Mth.lerp(ShakenStirClient.ANI_DELTAF * 0.5F, blockEntity.height, blockEntity.heightRate);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        SerializeHelper.loadVector2f(input, position);
        rotation = input.getFloatOr("Rot", 0.0f);
        model = input.getString("Model").map(Identifier::tryParse).orElse(null);
        pureName = input.getString("PureName").map(Component::translatable).orElse(null);
        contentComponents.clearPatch();
        DataComponentMap content = input.read("Content", DataComponentMap.CODEC).orElse(DataComponentMap.EMPTY);
        contentComponents.setAll(content);
        if (!contentComponents.isEmpty()) {
            heightRate = 1.0F;
            height = 1.0F;
            oHeight = 1.0F;
        }
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
        if (model != null) {
            output.putString("Model", model.toString());
        }
        if (pureName != null) {
            output.putString("PureName", pureName.getString());
        }
        if (!contentComponents.isEmpty()) {
            output.storeNullable("Content", DataComponentMap.CODEC, contentComponents);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        position.set(components.getOrDefault(DataComponentTypeRegistries.GLASSWARE_POSITION, new Vector2f()));
        rotation = components.getOrDefault(DataComponentTypeRegistries.GLASSWARE_ROTATION, 0.0f);
        if (components.has(DataComponents.ITEM_MODEL)) {
            model = components.get(DataComponents.ITEM_MODEL);
        }
        if (components.has(DataComponents.ITEM_NAME)) {
            pureName = components.get(DataComponents.ITEM_NAME);
        }
    }

    @SuppressWarnings("Convert2Lambda")
    public IModel<?> getModel() {
        return new Supplier<>() {
            @Override
            public IModel<?> get() {
                return Models.getModel(model).orElse(Models.COLLINS_GLASS);
            }}.get();
    }
}
