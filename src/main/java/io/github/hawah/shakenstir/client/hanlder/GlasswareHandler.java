package io.github.hawah.shakenstir.client.hanlder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.item.GlasswareItem;
import io.github.hawah.shakenstir.lib.client.KeyBinding;
import io.github.hawah.shakenstir.lib.client.handler.IHandler;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import io.github.hawah.shakenstir.util.IModel;
import io.github.hawah.shakenstir.util.Models;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class GlasswareHandler implements IHandler {
    @Override
    public void tick() {
        if (!isVisible()) {

        }

        if (!isActive()) {
            return;
        }


    }

    @Override
    public boolean isActive() {
        return getPlayer().getMainHandItem().getItem() instanceof GlasswareItem &&
                ClientDataHolder.Picker.block().isPresent() &&
                Direction.UP.equals(ClientDataHolder.Picker.direction()) &&
                KeyBinding.hasControlDown();
    }

    public static Player getPlayer() {
        return Minecraft.getInstance().player;
    }

    public static ItemStack getItem() {
        return getPlayer().getMainHandItem();
    }

    public static Optional<IModel> parseItemStack(ItemStack itemStack) {
        return itemStack.has(DataComponentTypeRegistries.CUSTOM_GLASSWARE_MODEL)?
                 Models.getModel(itemStack.get(DataComponentTypeRegistries.CUSTOM_GLASSWARE_MODEL)).map(m -> (IModel) m) :
                itemStack.has(DataComponentTypeRegistries.GLASSWARE_MODEL)?
                        Optional.of(Models.values()[itemStack.get(DataComponentTypeRegistries.GLASSWARE_MODEL)]) :
                        Optional.empty();
    }

    public IModel getModel() {
        return parseItemStack(getItem()).get();
    }

    public void submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, LevelRenderState levelRenderState) {
        if (!isActive())
            return;
        if (Minecraft.getInstance().hitResult == null) {
            return;
        }
        Vec3 location = Minecraft.getInstance().hitResult.getLocation();
        double x = location.x();
        double y = (int) location.y();
        double z = location.z();
        x = (Math.floor(x)) + Mth.clamp((x - Math.floor(x)), 0.25, 0.75);
        z = (Math.floor(z)) + Mth.clamp((z - Math.floor(z)), 0.25, 0.75);
        BlockPos pos = Minecraft.getInstance().hitResult instanceof BlockHitResult blockHitResult?
                blockHitResult.getBlockPos() :
                BlockPos.containing(location);
        Vec3 camPos = levelRenderState.cameraRenderState.pos;
        poseStack.pushPose();
        poseStack.translate(-camPos.x(), -camPos.y(), -camPos.z());
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.YN.rotationDegrees(levelRenderState.cameraRenderState.yRot));
        poseStack.translate(-0.5, 0, -0.5);
        int lightCoords = LevelRenderer.getLightCoords(Minecraft.getInstance().level, pos.above());
        final int RANGE = 50;
        float delta = (float) (Math.abs(((AnimationTickHolder.getRenderTime() % RANGE) / RANGE) - 0.5) * 2);
        int renderTime = Mth.lerpDiscrete(delta, 100, 200);
        getModel().submit(submitNodeCollector, poseStack, lightCoords, OverlayTexture.NO_OVERLAY, RenderTypes.translucentMovingBlock(), renderTime << 24 | 0xFFFFFF);
        poseStack.popPose();
    }
}
