package io.github.hawah.shakenstir.client.hanlder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.client.ClickInteractions;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.client.model.Models;
import io.github.hawah.shakenstir.client.render.general.GlasswareDecorations;
import io.github.hawah.shakenstir.client.render.general.GlasswareRenderer;
import io.github.hawah.shakenstir.content.block.Glassware;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.foundation.networking.ServerboundHandItemAmountChangedPacket;
import io.github.hawah.shakenstir.foundation.tags.SnsItemTags;
import io.github.hawah.shakenstir.lib.client.KeyBinding;
import io.github.hawah.shakenstir.lib.client.handler.IHandler;
import io.github.hawah.shakenstir.lib.networking.Networking;
import io.github.hawah.shakenstir.util.IModel;
import io.github.hawah.shakenstir.util.Result;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.hawah.shakenstir.client.hanlder.MC.*;

public class DecoratePlaceHandler implements IHandler {

    private double y = 0;
    private double deltaX = 0, deltaY = 0;
    private final Quaternionf quaternionf = new Quaternionf();

    public DecoratePlaceHandler() {
        ClickInteractions.registerMouseMove(this::onMouseMove);
    }

    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (KeyBinding.hasControlDown()) {
            assert mc.player != null;
            BlockPos pos = ClientDataHolder.Picker.pos();
            if (Minecraft.getInstance().level == null || pos == null || !(Minecraft.getInstance().level.getBlockEntity(pos) instanceof GlasswareBlockEntity blockEntity)) {
                return;
            }
            final Vec3 axis = mc.player.calculateViewVector(0.0f, mc.player.getYRot() - 90.0f - blockEntity.rotation);
            quaternionf.rotateLocalY((float)deltaX).premul(new Quaternionf(new AxisAngle4d(deltaY, axis.x(), axis.y(), axis.z())));
        }
        if (KeyBinding.hasTabDown()) {
            y = Mth.lerp(0.2, y, y + deltaY);
        }
    }

    @Override
    public boolean isActive() {
        return getPlayer() != null &&
                Minecraft.getInstance().level != null &&
                getItem().is(SnsItemTags.DRINK_DECORATION) &&
                Direction.UP.equals(ClientDataHolder.Picker.direction()) &&
                lookingAtCup();
    }

    public boolean lookingAtCup() {
        BlockPos pos = ClientDataHolder.Picker.pos();
        if (level() == null || pos == null) {
            return false;
        }
        return level().getBlockEntity(pos) instanceof GlasswareBlockEntity;
    }

    public void extract(ExtractLevelRenderStateEvent event) {
        if (!isActive()) {
            return;
        }
        LevelRenderState renderState = event.getRenderState();
        LevelRenderer levelRenderer = event.getLevelRenderer();
        ItemStack item = getItem();
        List<ItemModel> itemModels = new ArrayList<>();
        ClientLevel level = event.getLevel();
        VoxelShape shape = Shapes.empty();
        BlockPos pos = ClientDataHolder.Picker.pos();
        if (pos == null) {
            return;
        }
        GlasswareRenderer.ModelSelector selector = new GlasswareRenderer.ModelSelector();
        Identifier decorateModel;
        if ((item.has(DataComponentTypeRegistries.DECORATE_MODEL) && (decorateModel = item.get(DataComponentTypeRegistries.DECORATE_MODEL)) != null) ||
                (GlasswareDecorations.maps.containsKey(item.getItem()) && (decorateModel = GlasswareDecorations.maps.get(item.getItem())) != null)) {
            Optional<IModel<?>> model = Models.getModel(decorateModel);
            AtomicReference<VoxelShape> vs = new AtomicReference<>(shape);
            model.ifPresent(decoration -> {
                selector.select(decoration);
                vs.set(decoration.getShape());
            });
            shape = vs.get();
        } else if (item.is(SnsItemTags.BLOCK_LIKE_DRINK_DECORATION)) {
            BlockState state = ( (BlockItem) item.getItem()).getBlock().defaultBlockState();
            shape = state.getShape(level, pos);
            BlockModelRenderState blockModelRenderState = new BlockModelRenderState();
            Minecraft.getInstance().getBlockModelResolver().update(
                    blockModelRenderState,
                    state,
                    BlockDisplayContext.create()
            );
            selector.select(blockModelRenderState);
        } else if (item.is(SnsItemTags.ITEM_LIKE_DRINK_DECORATION)) {
            ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
            Minecraft.getInstance().getItemModelResolver().updateForTopItem(
                    itemStackRenderState,
                    item,
                    ItemDisplayContext.GROUND,
                    level,
                    null,
                    0
            );
            selector.select(itemStackRenderState);
        }
        renderState.setRenderData(RenderState.ctxKey, new RenderState(
                event.getCamera(),
                event.getDeltaTracker(),
                selector,
                LevelRenderer.getLightCoords(level, pos),
                shape
        ));
    }

    public void submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, LevelRenderState levelRenderState) {


        RenderState state = levelRenderState.getRenderData(RenderState.ctxKey);
        if (state == null) {
            return;
        }
        BlockPos pos = ClientDataHolder.Picker.pos();
        if (!(Minecraft.getInstance().level.getBlockEntity(pos) instanceof GlasswareBlockEntity blockEntity)) {
            return;
        }
        Vec3 camPos = state.camera.position();
        Vec3 location = ClientDataHolder.Picker.location();
        assert location != null;

        poseStack.pushPose();
        double size = state.shape.isEmpty()? 0.1 : state.shape.bounds().getSize();
        float scale = (float) (0.225F / size);
        poseStack.translate(-camPos.x(), -camPos.y(), -camPos.z());

        poseStack.translate(
                blockEntity.getVisualPosition().x() + pos.getX(),
                pos.getY(),
                pos.getZ() + blockEntity.getVisualPosition().y()
        );
        poseStack.mulPose(Axis.YN.rotationDegrees(blockEntity.rotation));

        Vec3 localPos = location.subtract(
                (blockEntity.getVisualPosition().x() + pos.getX()   ),
                (pos.getY()                              ),
                (blockEntity.getVisualPosition().y() + pos.getZ())  )
                .yRot((float) Math.toRadians(blockEntity.rotation));

        poseStack.translate(localPos.x(), localPos.y() + y, localPos.z());
        poseStack.mulPose(quaternionf);
        state.model.submit(
                poseStack,
                scale,
                submitNodeCollector,
                state::lightCord
        );
        poseStack.popPose();
    }

    public Result onMouseMove(final double yaw, final double pitch) {
        if (isActive() && (KeyBinding.hasControlDown() || KeyBinding.hasTabDown())) {
            deltaX = yaw / 500;
            deltaY = pitch / 200;
            tick();
            return new Result(true);
        }
        return Result.empty();
    }

    public boolean onMousePressed(final int button, final boolean down) {
        if (!isActive()) {
            return false;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && down) {
            return onRightDown();
        }
        return false;
    }

    public boolean onRightDown() {
        if (!ClientDataHolder.Picker.type().equals(HitResult.Type.BLOCK) || !ClientDataHolder.Picker.block().map(b -> b instanceof Glassware).orElse(false)) {
            return false;
        }
        BlockPos pos = ClientDataHolder.Picker.pos();
        if (pos == null) {
            return false;
        }
        if (level().getBlockEntity(pos) instanceof GlasswareBlockEntity blockEntity/* && blockEntity.hasContent()*/) {
            Vec3 location = ClientDataHolder.Picker.location().subtract(
                    blockEntity.getVisualPosition().x() + pos.getX(),
                    pos.getY() - y,
                    blockEntity.getVisualPosition().y() + pos.getZ()
            ).yRot((float) Math.toRadians(blockEntity.rotation));
            if (blockEntity.insertDecoration(new GlasswareBlockEntity.Decoration(location, new Quaternionf(quaternionf), getItem().copyWithCount(1)))) {
                getPlayer().swing(InteractionHand.MAIN_HAND);
                getItem().shrink(1);
                Networking.sendToServer(new ServerboundHandItemAmountChangedPacket(getItem().getCount(), getPlayer().getUUID(), InteractionHand.MAIN_HAND));
            }
            return true;
        }
        return false;
    }

    record RenderState(
            Camera camera,
            DeltaTracker deltaTracker,
            GlasswareRenderer.ModelSelector model,
            int lightCord,
            VoxelShape shape
    ) {
        public static final ContextKey<RenderState> ctxKey = ShakenStir.asContextKey("decorate_level");
    }
}
