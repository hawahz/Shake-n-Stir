package io.github.hawah.shakenstir.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.block.Shake;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.lib.client.render.EaseHelper;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import org.joml.*;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ShakeItemSpecialRenderer(Transformation transformation) implements SpecialModelRenderer<Vector2f> {

    public static final Matrix4f ARM_TRANSFORM = new Matrix4f(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5403024f, -0.84147114f, 0.0f, 0.0f, 0.84147114f, 0.5403024f, 0.0f, 0.9000001f, 0.12046755f, 0.95270944f, 1.0f);
    public static final Matrix4f SHAKING_ARM_TRANSFORM = new Matrix4f(0.59046054f, -0.0043104887f, -0.004017652f, 0.0f, 0.005534787f, 0.2675616f, 0.5263646f, 0.0f, -0.0020219171f, -0.52637595f, 0.2675891f, 0.0f, 0.08839527f, 1.0911926f, 0.32556745f, 1.0f);
    public static final Matrix4f SHAKING_SHAKE_TRANSFORM = new Matrix4f(0.99995023f, -0.0072998703f, -0.006803939f, 0.0f, 0.0040740552f, -0.32377687f, 0.94612664f, 0.0f, -0.0091095455f, -0.9461069f, -0.32373095f, 0.0f, 0.013291817f, 1.648548f, 0.02847635f, 1.0f);
    public static final Matrix4f LEFT_ARM_TRANSFORM = new Matrix4f(0.9210611f, -0.38941836f, -7.450581E-9f, 0.0f, 0.3874729f, 0.9164596f, -0.09983347f, 0.0f, 0.038876962f, 0.09195268f, 0.9950044f, 0.0f, -0.317625f, 0.6759194f, 0.2094838f, 1.0f);

    @Override
    public void submit(@Nullable Vector2f argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();

        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(BlockRegistries.SHAKE_BLOCK.get().defaultBlockState().setValue(Shake.FACING, Minecraft.getInstance().player.getMainHandItem().getOrDefault(DataComponentTypeRegistries.HAS_CUP, false)? Direction.UP: Direction.DOWN));
        List<BlockStateModelPart> list = new ArrayList<>();
        model.collectParts(RandomSource.create(), list);
//        TransformWarper warper = TransformWarper.instance(this);

        float renderTime = AnimationTickHolder.getRenderTime();
        float delta = renderTime % 60 - 10;

        boolean isShaking = ShakenStirClient.SHAKE_HANDLER.isActive();
        if (isShaking){
            float liftProcess = EaseHelper.easeOutPow(Mth.clamp((AnimationTickHolder.getRenderTime() - ShakenStirClient.SHAKE_HANDLER.firstTimeShake())/5, 0, 1), 2);
            if (liftProcess < 1) {
                poseStack.translate(0, (liftProcess-1) * 2, 0);
            }
            poseStack.translate(0, -0.2, 0);
            poseStack.translate(0, ShakenStirClient.SHAKE_HANDLER.y()/10, ShakenStirClient.SHAKE_HANDLER.x()/10);
            double mapping = (-ShakenStirClient.SHAKE_HANDLER.y() + 2)/4 * 1.5;
            poseStack.mulPose(Axis.XP.rotationDegrees((float) ((ShakenStirClient.SHAKE_HANDLER.x() - 1)/2 * 20 * (mapping))));
        } else {

        }

        poseStack.pushPose();

        if (isShaking) {
            poseStack.mulPose(SHAKING_SHAKE_TRANSFORM);
        }
        poseStack.mulPose(new Transformation(
                new Vector3f(0, 5.25F/16, -0.75F/16),
                new Quaternionf(),
                new Vector3f(0.74609F, 0.74609F, 0.74609F),
                new Quaternionf()
        ));
        poseStack.mulPose(transformation());

        submitNodeCollector.submitBlockModel(
                poseStack,
                RenderTypes.solidMovingBlock(),
                list,
                new int[]{0},
                lightCoords,
                overlayCoords,
                outlineColor
        );
        poseStack.popPose();

        poseStack.pushPose();

        if (isShaking) {
            poseStack.mulPose(SHAKING_ARM_TRANSFORM);
        }

        poseStack.mulPose(ARM_TRANSFORM);

        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        AvatarRenderer<AbstractClientPlayer> playerRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getPlayerRenderer(player);
        Identifier skinTexture = player.getSkin().body().texturePath();
        playerRenderer.renderRightHand(poseStack, submitNodeCollector, lightCoords, skinTexture, player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE), player);

        poseStack.popPose();

        if (isShaking) {
            poseStack.mulPose(LEFT_ARM_TRANSFORM);
            playerRenderer.renderLeftHand(poseStack, submitNodeCollector, lightCoords, skinTexture, player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE), player);
        }

//        warper.end();
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {

    }

    @Override
    public Vector2f extractArgument(ItemStack stack) {
        return new Vector2f();
    }

    public record Unbaked(Transformation transformation) implements SpecialModelRenderer.Unbaked<Vector2f> {
        public static final Codec<Transformation> TRANSFORMATION_CODEC = RecordCodecBuilder.create(
                i -> i.group(
                        ExtraCodecs.VECTOR3F.optionalFieldOf("translation", new Vector3f()).forGetter(Transformation::translation),
                        ExtraCodecs.VECTOR3F.optionalFieldOf("rotation", new Vector3f()).forGetter(t -> new Vector3f(t.rightRotation().x(), t.rightRotation().y(), t.rightRotation().z())),
                        ExtraCodecs.VECTOR3F.optionalFieldOf("scale", new Vector3f(1)).forGetter(Transformation::scale)
                ).apply(i, (tsl, rot, sc) -> new Transformation(((Vector3f) tsl).div(16), new Quaternionf(rot.x(), rot.y(), rot.z(), 1), sc, new Quaternionf()))
        );
        public static final MapCodec<ShakeItemSpecialRenderer.Unbaked> MAP_CODEC = TRANSFORMATION_CODEC.fieldOf("transform").xmap(Unbaked::new, Unbaked::transformation);
        @Override
        public ShakeItemSpecialRenderer bake(BakingContext context) {
            return new ShakeItemSpecialRenderer(transformation());
        }

        public Vector3f rot() {
            return new Vector3f(transformation().leftRotation().x(), transformation().leftRotation().y(), transformation().leftRotation().z());
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
