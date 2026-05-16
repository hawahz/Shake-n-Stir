package io.github.hawah.shakenstir.content.blockEntity;

import com.mojang.logging.LogUtils;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.client.model.Models;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.foundation.networking.ServerboundInsertDecorationPacket;
import io.github.hawah.shakenstir.lib.StreamCodecUtil;
import io.github.hawah.shakenstir.lib.networking.Networking;
import io.github.hawah.shakenstir.util.IModel;
import io.github.hawah.shakenstir.util.SerializeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GlasswareBlockEntity extends BlockEntity {

    // To Save
    public final Vector2f position = new Vector2f();
    public float rotation;
    // Item
    public Identifier model = null;
    public Component pureName = null;
    public Component defaultName = null;
    public PatchedDataComponentMap contentComponents = new PatchedDataComponentMap(DataComponentMap.EMPTY);

    public List<Decoration> decorationsList = new ArrayList<>();

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
        return (contentComponents.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFFFF))).rgb();
    }

    public boolean hasContent() {
        return !contentComponents.isEmpty() || true;
    }

    public boolean insertDecoration(Decoration decoration) {
        this.decorationsList.add(decoration);
        if (level.isClientSide()) {
            Networking.sendToServer(new ServerboundInsertDecorationPacket(decoration, worldPosition));
        }
        setChanged();
        return true;
    }

    public boolean pourProduct(ItemStack itemStack) {
        if (!this.components().isEmpty()) {
            return false;
        }
        contentComponents.setAll(itemStack.getComponents());
        heightRate = 1.0F;
        this.setChanged();
        if (getLevel() instanceof ServerLevel serverLevel){
            serverLevel.players().forEach(
                    player -> player.connection.send(getUpdatePacket())
            );
            net.neoforged.neoforge.attachment.AttachmentSync.syncBlockEntityUpdates(this, serverLevel.players());
        }
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
        pureName = input.read("PureName", ComponentSerialization.CODEC).orElse(null);
        defaultName = input.read("DefaultName", ComponentSerialization.CODEC).orElse(null);
        contentComponents.clearPatch();
        DataComponentMap content = input.read("Content", DataComponentMap.CODEC).orElse(DataComponentMap.EMPTY);
        contentComponents.setAll(content);
        if (!contentComponents.isEmpty()) {
            heightRate = 1.0F;
//            height = 1.0F;
//            oHeight = 1.0F;
        }
        Optional<ValueInput.ValueInputList> decorations = input.childrenList("Decorations");
        decorationsList.clear();
        if (decorations.isPresent()) {
            for (ValueInput valueInput : decorations.get()) {
                valueInput.read("Decoration", Decoration.CODEC).ifPresent(decorationsList::add);
            }
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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
            output.store("PureName", ComponentSerialization.CODEC, pureName);
        }
        if (!contentComponents.isEmpty()) {
            output.storeNullable("Content", DataComponentMap.CODEC, contentComponents);
        }
        if (defaultName != null) {
            output.store("DefaultName", ComponentSerialization.CODEC, defaultName);
        }
        ValueOutput.ValueOutputList decorations = output.childrenList("Decorations");
        for (Decoration decoration : decorationsList) {
            decorations.addChild().store("Decoration", Decoration.CODEC, decoration);
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
        if (components.has(DataComponentTypeRegistries.GLASSWARE_NAME)) {
            defaultName = components.get(DataComponentTypeRegistries.GLASSWARE_NAME);
        }

        if (components.has(DataComponentTypeRegistries.DRINK_DATA)) {
            this.contentComponents.clearPatch();;
            this.contentComponents.setAll(components.getOrDefault(DataComponentTypeRegistries.DRINK_DATA, DataComponentMap.EMPTY));
            if (!contentComponents.isEmpty()) {
                heightRate = 1.0F;
                height = 1.0F;
                oHeight = 1.0F;
            }
        }

        if (components.has(DataComponentTypeRegistries.GLASSWARE_DECORATIONS)) {
            this.decorationsList.addAll(components.getOrDefault(DataComponentTypeRegistries.GLASSWARE_DECORATIONS, List.of()));
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

    public record Decoration(Vec3 position, Quaternionf quaternionf, ItemStack itemStack) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Decoration> STREAM_CODEC = StreamCodec.composite(
                Vec3.STREAM_CODEC, Decoration::position,
                StreamCodecUtil.QUATERNION, Decoration::quaternionf,
                ItemStack.OPTIONAL_STREAM_CODEC, Decoration::itemStack,
                Decoration::new
        );

        public static final Codec<Decoration> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Vec3.CODEC.fieldOf("position").forGetter(Decoration::position),
                SerializeHelper.QUATERNIONF_CODEC.fieldOf("quaternionf").forGetter(Decoration::quaternionf),
                ItemStack.OPTIONAL_CODEC.fieldOf("itemStack").forGetter(Decoration::itemStack)
        ).apply(inst, Decoration::new));
    }
}
