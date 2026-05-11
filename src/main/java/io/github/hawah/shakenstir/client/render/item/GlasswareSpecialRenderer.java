package io.github.hawah.shakenstir.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class GlasswareSpecialRenderer implements SpecialModelRenderer<String> {
    @Override
    public void submit(@Nullable String argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {

    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {

    }

    @Override
    public @Nullable String extractArgument(ItemStack stack) {
        return "";
    }
}
