package io.github.hawah.shakenstir.lib.client.render;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.OptionalDouble;

public class OverRenderType {
    // 这是一个 dummy 构造器，只是为了能继承 RenderType 访问 protected 方法

    // 定义透视线框 RenderType
//    public static final RenderType OVERLAY_LINES = RenderType.create(
//            "overlay_lines",
//            DefaultVertexFormat.POSITION_COLOR_NORMAL,
//            VertexFormat.Mode.LINES,
//            256,
//            false,
//            false,
//            RenderSetup.builder(RenderPipelines.LINES)
//                    .setShaderState(RENDERTYPE_LINES_SHADER)
//                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
//                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
//                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
//                    .setOutputState(ITEM_ENTITY_TARGET)
//                    .setWriteMaskState(COLOR_DEPTH_WRITE)
//                    .setCullState(NO_CULL)
//                    .setDepthTestState(NO_DEPTH_TEST)
//                    .createCompositeState(false)
//    );
//
//    public static final RenderType OVERLAY_QUADS = RenderType.create(
//            "overlay_quads",
//            DefaultVertexFormat.POSITION_COLOR,
//            VertexFormat.Mode.QUADS,
//            256,
//            false,
//            true,
//            RenderType.CompositeState.builder()
//                    .setShaderState(POSITION_COLOR_SHADER)
//                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
//                    .setCullState(NO_CULL)
//                    .setDepthTestState(NO_DEPTH_TEST)
//                    .setWriteMaskState(COLOR_WRITE)
//                    .setOutputState(MAIN_TARGET)
//                    .createCompositeState(false)
//    );
}
