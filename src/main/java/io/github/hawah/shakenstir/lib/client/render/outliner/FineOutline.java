package io.github.hawah.shakenstir.lib.client.render.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FineOutline extends OutlineElement<FineOutline> {
    /**
     * 渲染选框
     * @param buffer     顶点消费者 (通常建议使用 MultiBufferSource.getBuffer(RenderType.lines()))
     * @param cameraPos  当前摄像机坐标 (用于平移到相对坐标)
     */
    @Override
    public void render(PoseStack.Pose pose, VertexConsumer buffer, Vec3 cameraPos, DeltaTracker partialTick) {

        // 构建 AABB 范围，确保 pos0 和 pos1 的大小关系正确
        AABB box = new AABB(visualPos0, visualPos1).inflate(0.002); // 稍微膨胀一点防止与方块表面闪烁 (Z-Fighting)
        PoseStack poseStack = new PoseStack();
        poseStack.last().set(pose);
        poseStack.pushPose();

        // 平移到相对于摄像机的坐标
        poseStack.translate(box.minX - cameraPos.x, box.minY - cameraPos.y, box.minZ - cameraPos.z);
        float delta = partialTick.getGameTimeDeltaPartialTick(true);

        int cr = (int) Mth.lerp(delta, or, r) * 255,
                cg = (int) Mth.lerp(delta, og, g) * 255,
                cb = (int) Mth.lerp(delta, ob, b) * 255,
                ca = (int) Mth.lerp(delta, oa, a) * 255;

        float width = (float) box.getXsize();
        float height = (float) box.getYsize();
        float depth = (float) box.getZsize();

        ShapeRenderer.renderShape(
                poseStack,
                buffer,
                Block.box(0, 0, 0, width * 16, height * 16, depth * 16),
                0,
                0,
                0,
                ARGB.color(ca, cr, cg, cb),
                Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth
        );

        poseStack.popPose();
    }
}
