package io.github.hawah.shakenstir.client.hanlder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.content.item.GlasswareItem;
import io.github.hawah.shakenstir.lib.client.KeyBinding;
import io.github.hawah.shakenstir.lib.client.handler.IHandler;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import io.github.hawah.shakenstir.util.IModel;
import io.github.hawah.shakenstir.client.model.Models;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class GlasswareHandler implements IHandler {

    private float oAlpha = 0;
    private float alpha = 0;
    private boolean active = false;
    private int runningTick = -1;
    private double x, y, z, oX, oY, oZ;

    @Override
    public void tick() {
        oAlpha = alpha;
        oX = x;
        oY = y;
        oZ = z;
        if (!isVisible()) {
            alpha = Mth.lerp(ShakenStirClient.ANI_DELTAF, alpha, 0);
        }

        if (!isActive()) {
            active = false;
            return;
        }

        Vec3 location = ClientDataHolder.Picker.hitResult().getLocation();
        double x = location.x();
        double y = (int) location.y();
        double z = location.z();
        x = (Math.floor(x)) + Mth.clamp((x - Math.floor(x)), 0.25, 0.75);
        z = (Math.floor(z)) + Mth.clamp((z - Math.floor(z)), 0.25, 0.75);

        double posDelta = (active)? ShakenStirClient.ANI_DELTAF: 1;
        this.x = Mth.lerp(posDelta, this.x, x);
        this.y = Mth.lerp(posDelta, this.y, y);
        this.z = Mth.lerp(posDelta, this.z, z);
        alpha = Mth.lerp(ShakenStirClient.ANI_DELTAF, alpha, 1);

        if (posDelta == 1) {
            oX = x;
            oY = y;
            oZ = z;
        }

        if (!active) {
            runningTick = AnimationTickHolder.getTicks();
        }


        active = true;

    }

    @Override
    public boolean isActive() {
        if (PACKAGE.getPlayer() == null) {
            return false;
        }
        BlockState state;
        BlockPos pos;
        return PACKAGE.getPlayer().getMainHandItem().getItem() instanceof GlasswareItem &&
                ClientDataHolder.Picker.block().isPresent() &&
                Direction.UP.equals(ClientDataHolder.Picker.direction()) &&
                (pos = ClientDataHolder.Picker.pos()) != null &&
                Minecraft.getInstance().level != null &&
                (state = ClientDataHolder.Picker.blockState().orElse(null)) != null &&
                state.isSolidRender() &&
                state.isFaceSturdy(Minecraft.getInstance().level, pos, Direction.UP) &&
                Minecraft.getInstance().level.getBlockState(pos.above()).isEmpty() &&
                KeyBinding.hasControlDown();
    }



    public static Optional<IModel<?>> parseItemStack(ItemStack itemStack) {
        return itemStack.has(DataComponents.ITEM_MODEL)?
                Models.getModel(itemStack.get(DataComponents.ITEM_MODEL)) :
                Optional.of(Models.COLLINS_GLASS);
    }

    public IModel<?> getModel() {
        return parseItemStack(PACKAGE.getItem()).orElse(Models.MARGARITA_GLASS);
    }

    public void submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, LevelRenderState levelRenderState) {
        if (this.alpha <= 0.001)
            return;
        if (ClientDataHolder.Picker.hitResult() == null) {
            return;
        }
        GlasswareHandlerRenderState renderData = levelRenderState.getRenderData(GlasswareHandlerRenderState.ctxKey);
        if (renderData == null) {
            return;
        }
        float partialTick = renderData.deltaTracker().getGameTimeDeltaPartialTick(false);
        float pastTime = AnimationTickHolder.getRenderTime() - runningTick;
        float cAlpha = Mth.lerp(    partialTick, oAlpha, alpha);
        float x = (float) Mth.lerp( partialTick, oX, this.x);
        float y = (float) Mth.lerp( partialTick, oY, this.y);
        float z = (float) Mth.lerp( partialTick, oZ, this.z);
        BlockPos pos = Optional.ofNullable(ClientDataHolder.Picker.pos()).orElse(BlockPos.containing(x, y, z));
        Vec3 camPos = levelRenderState.cameraRenderState.pos;

        poseStack.pushPose();
        poseStack.translate(-camPos.x(), -camPos.y(), -camPos.z());
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.YN.rotationDegrees(levelRenderState.cameraRenderState.yRot));
        poseStack.translate(-0.5, 0, -0.5);

        assert Minecraft.getInstance().level != null;
        int lightCoords = LevelRenderer.getLightCoords(Minecraft.getInstance().level, pos.above());
        final int RANGE = 50;
        float delta = (float) (Math.abs(((pastTime % RANGE) / RANGE) - 0.5) * 2);
        int renderTime = (int) (Mth.lerpDiscrete(delta, 60, 140) * cAlpha);
        getModel().submit(submitNodeCollector, poseStack, lightCoords, OverlayTexture.NO_OVERLAY, RenderTypes.translucentMovingBlock(), renderTime << 24 | 0xFFFFFF);
        poseStack.popPose();
    }
}
