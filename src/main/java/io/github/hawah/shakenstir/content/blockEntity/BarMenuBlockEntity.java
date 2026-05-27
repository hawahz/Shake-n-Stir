package io.github.hawah.shakenstir.content.blockEntity;

import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BarMenuBlockEntity extends AutoUpdateBlockEntity {

    public BarMenuBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityRegistries.BAR_MENU_BLOCK_ENTITY.get(), worldPosition, blockState);
    }

    private UUID placerId = null;
    public List<FormattedText> content = new ArrayList<>();

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        placerId = input.read("PlacerId", UUIDUtil.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (placerId != null) {
            output.store("PlacerId", UUIDUtil.CODEC, placerId);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        placerId = components.get(DataComponentTypeRegistries.PLACER);
    }

    public UUID getPlacerId() {
        return placerId;
    }
}
