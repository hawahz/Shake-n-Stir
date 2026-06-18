package io.github.hawah.shakenstir.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.content.block.SpiritBlock;
import io.github.hawah.shakenstir.lib.client.render.EaseHelper;
import io.github.hawah.shakenstir.lib.client.render.toolkit.Animation;
import io.github.hawah.shakenstir.lib.client.render.toolkit.AnimationPlayer;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SpiritBottleSpecialRenderer implements SpecialModelRenderer<SpiritBottleSpecialRenderer.RenderState> {
    public static final int POWER = 2;

    public AnimationPlayer animationPlayer = new AnimationPlayer();

    @SuppressWarnings("UnusedAssignment")
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

    @Override
    public void submit(@Nullable RenderState argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (argument == null || !(argument.itemStack().getItem() instanceof BlockItem blockItem) || blockItem.getBlock().defaultBlockState().getOptionalValue(SpiritBlock.FACING).isEmpty()) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        poseStack.pushPose();
        BlockModelResolver blockModelResolver = Minecraft.getInstance().getBlockModelResolver();
        BlockModelRenderState blockModelRenderState = new BlockModelRenderState();
        blockModelResolver.update(blockModelRenderState, blockItem.getBlock().defaultBlockState().setValue(SpiritBlock.FACING, Direction.NORTH), BlockDisplayContext.create());


        float cooldownPercent = player.getCooldowns().getCooldownPercent(player.getMainHandItem(), AnimationTickHolder.getPartialTicks());
        float process = (1-cooldownPercent) * 20;


        if (argument.local() && argument.localFirstPerson()){
            float swingY = ((Double) animationPlayer.getAnimation("swingY").value(process)).floatValue();
            float swingZRot = ((Double) animationPlayer.getAnimation("swingZRot").value(process)).floatValue();

            poseStack.translate(0, swingY, -0.2);
            poseStack.mulPose(new Quaternionf(0, 0, swingZRot, 1));
        }
        blockModelRenderState.submit(
                poseStack,
                submitNodeCollector,
                lightCoords,
                overlayCoords,
                outlineColor
        );
        poseStack.popPose();

    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {

    }



    @Override
    public @Nullable RenderState extractArgument(ItemStack stack) {
        if (Minecraft.getInstance().player != null) {
            boolean local = Minecraft.getInstance().player.getInventory().contains(stack);
            return new RenderState(
                    local,
                    Minecraft.getInstance().options.getCameraType().isFirstPerson(),
                    stack
            );
        }
        return null;
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<RenderState> {
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

    public record RenderState(boolean local, boolean localFirstPerson, ItemStack itemStack){}
}
