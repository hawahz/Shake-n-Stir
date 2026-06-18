package io.github.hawah.shakenstir.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import io.github.hawah.shakenstir.foundation.fluid.TintColorGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClientFluidColorGetter {

    private static final ConcurrentMap<Fluid, Integer> cachedColor = new ConcurrentHashMap<>();

    public static int getColor(FluidStack fluidStack) {
        if (fluidStack.getFluidType() instanceof TintColorGetter tcg) {
            return tcg.getTintColor();
        }
        Fluid fluid = fluidStack.getFluid();
        boolean couldAvoidCalculate = cachedColor.containsKey(fluid);
        if (couldAvoidCalculate) {
            return cachedColor.getOrDefault(fluid, -1);
        }
        int color;
        int pixelRGBA = 0;
        try {
            BlockState legacyBlock = fluid.defaultFluidState().createLegacyBlock();
            BlockModelRenderState state = new BlockModelRenderState();
            Minecraft.getInstance().getBlockModelResolver().update(
                    state,
                    legacyBlock,
                    BlockDisplayContext.create()
            );
            var dummy = new DummySubmitNodeCollector();
            state.submit(new PoseStack(), dummy, 0, 0, 0);

            List<BlockStateModelPart> output = dummy.blockParts;
            if (output != null && !output.isEmpty()) {
                TextureAtlasSprite sprite = output.getFirst().particleMaterial().sprite();
                int[] pixels = sprite.contents().getOriginalImage().getPixels();
                double a = 0, r = 0, g = 0, b = 0;
                for (int pixel : pixels) {
                    a += (pixel >> 24) & 0xFF;
                    r += (pixel >> 16) & 0xFF;
                    g += (pixel >> 8) & 0xFF;
                    b += pixel & 0xFF;
                }
                double count = pixels.length;
                pixelRGBA = ((int) (a / count) << 24)
                        | ((int) (r / count) << 16)
                        | ((int) (g / count) << 8)
                        |  (int) (b / count);
            }
        } catch (RuntimeException e) {
            LogUtils.getLogger().error("Failed to get texture for fluid.", e);
        }
        color = pixelRGBA;
        cachedColor.put(fluid, color);
        return color;
    }
}
