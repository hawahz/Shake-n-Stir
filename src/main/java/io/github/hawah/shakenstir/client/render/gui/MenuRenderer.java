package io.github.hawah.shakenstir.client.render.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

public class MenuRenderer extends PictureInPictureRenderer<MenuRenderState> {

    private final Font font;

    public MenuRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
        font = Minecraft.getInstance().font;
    }

    @Override
    public Class<MenuRenderState> getRenderStateClass() {
        return MenuRenderState.class;
    }

    @Override
    protected void renderToTexture(MenuRenderState renderState, PoseStack poseStack) {
//        TextureTarget textureTarget = new TextureTarget("bar_menu_texture_target", 256, 256, true);
//        GpuTexture colorTexture = textureTarget.getColorTexture();
//        if (RenderSystem.outputColorTextureOverride != null) {
//            textureTarget.blitAndBlendToTexture(RenderSystem.outputColorTextureOverride);
//        }
    }

    @Override
    protected String getTextureLabel() {
        return "Test";
    }
}
