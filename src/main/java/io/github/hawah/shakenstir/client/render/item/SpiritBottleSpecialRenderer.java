package io.github.hawah.shakenstir.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.block.SpiritBlock;
import io.github.hawah.shakenstir.lib.client.render.EaseHelper;
import io.github.hawah.shakenstir.lib.client.render.toolkit.Animation;
import io.github.hawah.shakenstir.lib.client.render.toolkit.AnimationPlayer;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.joml.*;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SpiritBottleSpecialRenderer implements SpecialModelRenderer<Vector2f> {
    public static final Matrix4f ARM_TRANSFORM = new Matrix4f(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5403024f, -0.84147114f, 0.0f, 0.0f, 0.84147114f, 0.5403024f, 0.0f, 0.9000001f, 0.12046755f, 0.95270944f, 1.0f);
    public static final int POWER = 2;

    public AnimationPlayer animationPlayer = new AnimationPlayer();

    public SpiritBottleSpecialRenderer() {
        Animation<Double> swingY = animationPlayer.registerAnimation("swingY", 0.0);
        int i = 4;
        swingY.addKeyFrame(0.0, 0.0)
                .withMapping(v-> (double) EaseHelper.easeOutBounce(v.floatValue()));
        swingY.addKeyFrame(i+=2, 0.2);// 4
        swingY.addKeyFrame(i+=2, 0.2)// 6
                .withMapping(v -> (double) EaseHelper.easeOutPow(v.floatValue(), POWER));//
        swingY.addKeyFrame(i+=2, 0.3)//8
                .withMapping(v -> (double) EaseHelper.easeOutPow(v.floatValue(), POWER));//
        swingY.addKeyFrame(i+=2, 0.2)//10
                .withMapping(v -> (double) EaseHelper.easeOutPow(v.floatValue(), POWER));//
        swingY.addKeyFrame(i+=2, 0.3)//12
                .withMapping(v -> (double) EaseHelper.easeOutPow(v.floatValue(), POWER));//
        swingY.addKeyFrame(i+=2, 0.2);//14
        swingY.addKeyFrame(20, 0.0);

        i = 4;
        Animation<Double> swingZRot = animationPlayer.registerAnimation("swingZRot", 0.0);
        swingZRot.addKeyFrame(0.0, 0.0).withMapping(v-> (double) EaseHelper.easeOutBounce(v.floatValue()));
        swingZRot.addKeyFrame(i+=2, 0.0);//4
        swingZRot.addKeyFrame(i+=2, 0.1)//6
                .withMapping(v -> (double) EaseHelper.easeOutPow(v.floatValue(), POWER));//
        swingZRot.addKeyFrame(i+=2, -0.1)//8
                .withMapping(v -> (double) EaseHelper.easeOutPow(v.floatValue(), POWER));//
        swingZRot.addKeyFrame(i+=2, 0.1)//10
                .withMapping(v -> (double) EaseHelper.easeOutPow(v.floatValue(), POWER));//
        swingZRot.addKeyFrame(i+=2, -0.1)//12
                .withMapping(v -> (double) EaseHelper.easeOutPow(v.floatValue(), POWER));//
        swingZRot.addKeyFrame(i+=2, 0.1);//14
        swingZRot.addKeyFrame(20, 0.0);

    }

    private float storage = 0;

    @Override
    public void submit(@Nullable Vector2f argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (!(Minecraft.getInstance().player.getMainHandItem().getItem() instanceof BlockItem blockItem) || blockItem.getBlock().defaultBlockState().getOptionalValue(SpiritBlock.FACING).isEmpty()) {
            return;
        }
        poseStack.pushPose();
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockItem.getBlock().defaultBlockState().setValue(SpiritBlock.FACING, Direction.NORTH));
        List<BlockStateModelPart> list = new ArrayList<>();
        model.collectParts(RandomSource.create(), list);


        float cooldownPercent = Minecraft.getInstance().player.getCooldowns().getCooldownPercent(Minecraft.getInstance().player.getMainHandItem(), AnimationTickHolder.getPartialTicks());
        float process = (1-cooldownPercent) * 20;


        float swingY = ((Double) animationPlayer.getAnimation("swingY").value(process)).floatValue();
        float swingZRot = ((Double) animationPlayer.getAnimation("swingZRot").value(process)).floatValue();


        poseStack.translate(0, swingY, -0.2);
        poseStack.mulPose(new Quaternionf(0, 0, swingZRot, 1));


        poseStack.pushPose();
        poseStack.pushPose();
        poseStack.mulPose(new Transformation(
                new Vector3f(0, 5.25F/16, -0.75F/16),
                new Quaternionf(),
                new Vector3f(0.74609F, 0.74609F, 0.74609F),
                new Quaternionf()
        ));
        poseStack.mulPose(new Matrix4f(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.1f, 0.10000002f, -0.3f, 1.0f));

        submitNodeCollector.submitBlockModel(
                poseStack,
                RenderTypes.translucentMovingBlock(),
                list,
                new int[]{0},
                lightCoords,
                overlayCoords,
                outlineColor
        );

        poseStack.popPose();


        poseStack.mulPose(new Matrix4f(-0.1962016f, -0.7739172f, -0.6021288f, 0.0f, 0.037675902f, 0.6076606f, -0.79330355f, 0.0f, 0.97984076f, -0.17833316f, -0.09006579f, 0.0f, 0.5329359f, -0.08887535f, 0.49872145f, 1.0f));

        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        AvatarRenderer<AbstractClientPlayer> playerRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getPlayerRenderer(player);
        Identifier skinTexture = player.getSkin().body().texturePath();
        playerRenderer.renderRightHand(poseStack, submitNodeCollector, lightCoords, skinTexture, player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE), player);

        poseStack.popPose();
        poseStack.popPose();

    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {

    }



    @Override
    public @Nullable Vector2f extractArgument(ItemStack stack) {
        return null;
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<Vector2f> {
        public static final MapCodec<SpiritBottleSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());
        @Override
        public SpiritBottleSpecialRenderer bake(BakingContext context) {
            return new SpiritBottleSpecialRenderer();
        }

        @Override
        public MapCodec<SpiritBottleSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
