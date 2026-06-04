package io.github.hawah.shakenstir.content.blockEntity;

import com.mojang.logging.LogUtils;
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
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GlasswareBlockEntity extends AutoUpdateBlockEntity {

    // To Save
    public final Vector2f positionRate = new Vector2f();
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
    public final Vector2f position = new Vector2f();
    public float height = 0;
    // End

    public GlasswareBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityRegistries.GLASSWARE_BLOCK_ENTITY.get(), worldPosition, blockState);
    }

    public int getColor() {
        return (contentComponents.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFFFF))).rgb();
    }

    public boolean hasContent() {
        return !contentComponents.isEmpty();
    }

    public boolean insertDecoration(Decoration decoration) {
        if (level == null) {
            LogUtils.getLogger().warn("Cannot insert decoration to non level block entity");
            return false;
        }
        this.decorationsList.add(decoration);
        if (level.isClientSide()) {
            Networking.sendToServer(new ServerboundInsertDecorationPacket(decoration, worldPosition));
        }
        markChanged();
        return true;
    }

    public void moveTo(float localX, float localY) {
        positionRate.set(localX, localY);
        markChanged();
    }

    public boolean pourProduct(ItemStack itemStack) {
        if (!this.contentComponents.isEmpty()) {
            return false;
        }
        contentComponents.setAll(itemStack.getComponents());
        heightRate = 1.0F;
        this.markChanged();
        if (getLevel() instanceof ServerLevel serverLevel){
            serverLevel.players().forEach(
                    player -> player.connection.send(getUpdatePacket())
            );
            // FIXME
            //noinspection UnstableApiUsage
            net.neoforged.neoforge.attachment.AttachmentSync.syncBlockEntityUpdates(this, serverLevel.players());
        }
        return true;
    }

    public static void onAnimationTick(Level ignoredLevel, BlockPos ignoredPos, BlockState ignoredState, GlasswareBlockEntity blockEntity) {
        blockEntity.oHeight = blockEntity.height;
        blockEntity.height = Mth.lerp(ShakenStirClient.ANI_DELTAF * 0.5F, blockEntity.height, blockEntity.heightRate);

        blockEntity.oPosition.set(blockEntity.position);
        blockEntity.position.lerp(blockEntity.positionRate, ShakenStirClient.ANI_DELTAF * 0.2F);
    }

    public Vector2f getVisualPosition() {
        return position;
    }


    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        SerializeHelper.loadVector2f(input, positionRate);
        if (input.getInt("position").isPresent()) {
            SerializeHelper.loadVector2fNamed(input, position, "position");
        }
        rotation = input.getFloatOr("Rot", 0.0f);
        model = input.getString("Model").map(Identifier::tryParse).orElse(null);
        pureName = input.read("PureName", ComponentSerialization.CODEC).orElse(null);
        defaultName = input.read("DefaultName", ComponentSerialization.CODEC).orElse(null);
        contentComponents.clearPatch();
        DataComponentMap content = input.read("Content", DataComponentMap.CODEC).orElse(DataComponentMap.EMPTY);
        contentComponents.setAll(content);
        if (!contentComponents.isEmpty()) {
            heightRate = 1.0F;
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        SerializeHelper.saveVector2f(output, positionRate);
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
        if (oPosition.equals(position)) {
            SerializeHelper.saveVector2fNamed(output, position, "position");
        }
        ValueOutput.ValueOutputList decorations = output.childrenList("Decorations");
        for (Decoration decoration : decorationsList) {
            decorations.addChild().store("Decoration", Decoration.CODEC, decoration);
        }
    }

    // TODO 现在有无内容物的判断已经不能用contentComponents了

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        positionRate.set(components.getOrDefault(DataComponentTypeRegistries.GLASSWARE_POSITION, new Vector2f()));
        position.set(positionRate);
        oPosition.set(positionRate);
        rotation = components.getOrDefault(DataComponentTypeRegistries.GLASSWARE_ROTATION, 0.0f);
        if (components.has(DataComponentTypeRegistries.DRINK_DATA)) {
            this.contentComponents.clearPatch();
            this.contentComponents.set(DataComponentTypeRegistries.DRINK_DATA, components.get(DataComponentTypeRegistries.DRINK_DATA));
        }

        if (components.has(DataComponents.DYED_COLOR) && !contentComponents.isEmpty()) {
            this.contentComponents.set(DataComponents.DYED_COLOR, components.get(DataComponents.DYED_COLOR));
        }

        if (components.has(DataComponentTypeRegistries.SHAKE_PRODUCT_QUALITY) && !contentComponents.isEmpty()) {
            this.contentComponents.set(DataComponentTypeRegistries.SHAKE_PRODUCT_QUALITY, components.get(DataComponentTypeRegistries.SHAKE_PRODUCT_QUALITY));
        }

        if (components.has(DataComponents.ITEM_MODEL)) {
            model = components.get(DataComponents.ITEM_MODEL);
        }
        if (components.has(DataComponents.ITEM_NAME)) {
            pureName = components.get(DataComponents.ITEM_NAME);
            if (!contentComponents.isEmpty()){
                contentComponents.set(DataComponents.ITEM_NAME, pureName);
            }
        }
        if (components.has(DataComponentTypeRegistries.GLASSWARE_NAME)) {
            defaultName = components.get(DataComponentTypeRegistries.GLASSWARE_NAME);
        }


        if (!contentComponents.isEmpty()) {
            heightRate = 1.0F;
            height = 1.0F;
            oHeight = 1.0F;
        }

        if (components.has(DataComponentTypeRegistries.GLASSWARE_DECORATIONS)) {
            this.decorationsList.addAll(components.getOrDefault(DataComponentTypeRegistries.GLASSWARE_DECORATIONS, List.of()));
        }
    }

    public IModel<?> getModel() {
        return Models.getModel(model).orElse(Models.COLLINS_GLASS);
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
