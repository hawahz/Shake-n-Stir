package io.github.hawah.shakenstir.client.hanlder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.client.ClickInteractions;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.content.block.Glassware;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.foundation.tags.SnsItemTags;
import io.github.hawah.shakenstir.lib.client.KeyBinding;
import io.github.hawah.shakenstir.lib.client.handler.IHandler;
import io.github.hawah.shakenstir.util.Result;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
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

import static io.github.hawah.shakenstir.client.hanlder.PACKAGE.*;

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
            if (!(Minecraft.getInstance().level.getBlockEntity(pos) instanceof GlasswareBlockEntity blockEntity)) {
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
        List<BlockStateModelPart> list = new ArrayList<>();
        List<ItemModel> itemModels = new ArrayList<>();
        ClientLevel level = event.getLevel();
        VoxelShape shape = Shapes.empty();
        BlockPos pos = ClientDataHolder.Picker.pos();
        if (pos == null) {
            return;
        }
        if ((item.getItem() instanceof BlockItem blockItem)) {
            BlockState state = blockItem.getBlock().defaultBlockState();
            shape = state.getShape(level, pos);
            BlockStateModel blockModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
            blockModel.collectParts(level.getRandom(), list);
        } else {
            Identifier modelId = item.get(DataComponents.ITEM_MODEL);
            if (modelId != null) {
                ItemModel itemModel = Minecraft.getInstance().getModelManager().getItemModel(modelId);
                itemModels.add(itemModel);
                shape = Shapes.box(0, 0, 0, .5, .5, .5);
            }
        }
        renderState.setRenderData(RenderState.ctxKey, new RenderState(
                event.getCamera(),
                event.getDeltaTracker(),
                list,
                itemModels,
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
        double size = state.shape.bounds().getSize();
        float scale = (float) (0.225F / size);
        poseStack.translate(-camPos.x(), -camPos.y(), -camPos.z());

        poseStack.translate(
                blockEntity.position.x() + pos.getX(),
                pos.getY(),
                pos.getZ() + blockEntity.position.y()
        );
        poseStack.mulPose(Axis.YN.rotationDegrees(blockEntity.rotation));

        Vec3 localPos = location.subtract(
                (blockEntity.position.x() + pos.getX()   ),
                (pos.getY()                              ),
                (blockEntity.position.y() + pos.getZ())  )
                .yRot((float) Math.toRadians(blockEntity.rotation));

        poseStack.translate(localPos.x(), localPos.y() + y, localPos.z());
        poseStack.mulPose(quaternionf);
        if (!state.model().isEmpty()){
            poseStack.translate(-0.5 * scale, 0 * scale, -0.5 * scale);
            poseStack.scale(scale, scale, scale);

            submitNodeCollector.submitBlockModel(
                    poseStack,
                    RenderTypes.translucentMovingBlock(),
                    state.model(),
                    new int[]{0},
                    state.lightCord,
                    OverlayTexture.NO_OVERLAY,
                    0
            );
        } else if (!state.itemModels().isEmpty()) {
            for (ItemModel itemModel : state.itemModels()) {
                poseStack.scale(scale, scale, scale);
                ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
                itemModel.update(
                        itemStackRenderState,
                        getItem(),
                        Minecraft.getInstance().getItemModelResolver(),
                        ItemDisplayContext.NONE,
                        null,
                        null,
                        0
                );
                itemStackRenderState.submit(
                        poseStack,
                        submitNodeCollector,
                        state.lightCord,
                        OverlayTexture.NO_OVERLAY,
                        0
                );
            }
        }

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
        if (level().getBlockEntity(pos) instanceof GlasswareBlockEntity blockEntity && blockEntity.hasContent()) {
            Vec3 location = ClientDataHolder.Picker.location().subtract(
                    blockEntity.position.x() + pos.getX(),
                    pos.getY() - y,
                    blockEntity.position.y() + pos.getZ()
            ).yRot((float) Math.toRadians(blockEntity.rotation));
            blockEntity.insertDecoration(new GlasswareBlockEntity.Decoration(location, new Quaternionf(quaternionf), getItem().copyWithCount(1)));
        }
        return false;
    }

    record RenderState(
            Camera camera,
            DeltaTracker deltaTracker,
            List<BlockStateModelPart> model,
            List<ItemModel> itemModels,
            int lightCord,
            VoxelShape shape
    ) {
        public static final ContextKey<RenderState> ctxKey = ShakenStir.asContextKey("decorate_level");
    }
}
