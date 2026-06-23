package io.github.hawah.shakenstir.lib.client.render.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class Outliner {
    private static Outliner INSTANCE = null;

    private final ConcurrentHashMap<Object, OutlineElement<?>> outlines = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Object, OutlineElement<?>> overOutlines = new ConcurrentHashMap<>();

    public static boolean hasInstance() {
        return INSTANCE != null;
    }

    public static Outliner getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Outliner();
        }
        return INSTANCE;
    }

    public static void renderInto(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos, DeltaTracker partialTick) {
//        if (INSTANCE == null) {
//            return;
//        }
//        INSTANCE.render(poseStack, bufferSource, cameraPos, partialTick);
//        INSTANCE.renderOverlay(poseStack, bufferSource, cameraPos, partialTick);
    }

    public OutlineElement<?> thickBox(Object slot) {
        var slotHolder = outlines.containsKey(slot)?
                outlines :
                overOutlines.containsKey(slot)?
                        overOutlines :
                        null;
        if (slotHolder == null) {
            return new ThickOutline();
        }
        OutlineElement<?> outlineElement = slotHolder.get(slot);
        if (!(outlineElement instanceof ThickOutline)) {
            LogUtils.getLogger().warn("Outline element is not a ThickOutline at thickBox()");
        }
        return outlineElement;
    }

    public OutlineElement<?> chaseThickBox(Object slot, BlockPos first, BlockPos second) {
        return chaseThickBox(slot, first, second, false);
    }

    public OutlineElement<?> chaseThickBox(Object slot, BlockPos first, BlockPos second, boolean overlay) {
        var slotHolder = overlay? overOutlines: outlines;
        if (slotHolder.containsKey(slot)) {
            OutlineElement<?> outline = slotHolder.get(slot);
            if (!(outline instanceof ThickOutline)) {
                LogUtils.getLogger().warn("Outline element is not a ThickOutline at chaseThickBox()   ");
            }
            return mulPose(first, second, outline);
        }
        ThickOutline outline = new ThickOutline();
        slotHolder.put(slot, mulPose(first, second, outline));
        return outline;
    }

    public void render(PoseStack.Pose poseStack, MultiBufferSource bufferSource, Vec3 cameraPos, DeltaTracker partialTick) {
        outlines.forEach((_, outlineElement) ->
                outlineElement.render(poseStack, bufferSource.getBuffer(
                        outlineElement instanceof ThickOutline?
                                getThickRenderType() :
                                RenderTypes.lines()
                ), cameraPos, partialTick)
        );
    }

    private static RenderType getThickRenderType() {

        return RenderTypes.debugFilledBox();
    }

    public void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos, DeltaTracker partialTick) {
//        overOutlines.forEach((object, outlineElement) ->
//                outlineElement.render(poseStack, bufferSource.getBuffer(
//                        outlineElement instanceof ThickOutline?
//                                OverRenderType.OVERLAY_QUADS :
//                                OverRenderType.OVERLAY_LINES
//                ), cameraPos, partialTick)
//        );
    }

    public OutlineElement<?> chaseBox(Object slot, BlockPos first, BlockPos second) {
        return chaseBox(slot, first, second, false);
    }

    public OutlineElement<?> chaseBox(Object slot, BlockPos first, BlockPos second, boolean overlay) {
        var slotHolder = overlay? overOutlines: outlines;
        if (slotHolder.containsKey(slot)) {
            OutlineElement<?> outline = slotHolder.get(slot);
            if (!(outline instanceof FineOutline)) {
                LogUtils.getLogger().warn("Outline element is not a FineOutline at chaseBox()");
            }
            return mulPose(first, second, outline);
        }
        FineOutline outline = new FineOutline();
        slotHolder.put(slot, mulPose(first, second, outline));
        return outline;
    }

    public OutlineElement<?> box(Object slot) {
        var slotHolder = outlines.containsKey(slot)?
                outlines :
                overOutlines.containsKey(slot)?
                        overOutlines :
                        null;
        if (slotHolder == null) {
            return new FineOutline();
        }
        OutlineElement<?> outlineElement = slotHolder.get(slot);
        if (!(outlineElement instanceof FineOutline)) {
            LogUtils.getLogger().warn("Outline element is not a FineOutline at box()");
        }
        return outlineElement;
    }

    @NotNull
    private OutlineElement<?> mulPose(BlockPos first, BlockPos second, OutlineElement<?> outline) {
        outline.setPositions(
                new Vec3(
                        Math.min(first.getX(), second.getX()),
                        Math.min(first.getY(), second.getY()),
                        Math.min(first.getZ(), second.getZ())
                ),
                new Vec3(
                        Math.max(first.getX(), second.getX()) + 1.0,
                        Math.max(first.getY(), second.getY()) + 1.0,
                        Math.max(first.getZ(), second.getZ()) + 1.0
                ));
        return outline;
    }

    public static void tick() {
        if (!hasInstance()) {
            return;
        }
        List<Object> slotsToRemove = new ArrayList<>();
        INSTANCE.outlines.forEach((object, outlineElement) -> {
            outlineElement.tick();
            if (Math.abs(outlineElement.oa) < 0.01 && outlineElement.discarded) {
                slotsToRemove.add(object);
            }
        });
        INSTANCE.overOutlines.forEach((object, outlineElement) -> {
            outlineElement.tick();
            if (Math.abs(outlineElement.oa) < 0.01 && outlineElement.discarded) {
                slotsToRemove.add(object);
            }
        });
        slotsToRemove.forEach(object -> INSTANCE.clearSlot(object));
    }

    public void clearSlot(Object slot) {
        outlines.computeIfPresent(slot, (object, outlineElement) -> outlineElement.discard());
        outlines.remove(slot);
        overOutlines.computeIfPresent(slot, (object, outlineElement) -> outlineElement.discard());
        overOutlines.remove(slot);
    }

    public void clear() {
        outlines.clear();
        overOutlines.clear();
    }

    public void updateOutlinePosition(Object slot, Vec3 p0, Vec3 p1) {
        if (outlines.containsKey(slot)) {
            outlines.get(slot).setPositions(p0, p1);
        } else {
            ThickOutline outline = new ThickOutline();
            outline.setPositions(p0, p1);
            outlines.put(slot, outline);
        }
    }

    public void updateOutlineColor(Object slot, float r, float g, float b, float a) {
        if (outlines.containsKey(slot)) {
            outlines.get(slot).setRGBA(r, g, b, a);
        } else {
            ThickOutline outline = new ThickOutline();
            outline.setRGBA(r, g, b, a);
            outlines.put(slot, outline);
        }
    }

    public static void extract(ExtractLevelRenderStateEvent event) {
        event.getRenderState().setRenderData(OUTLINER_RENDER_STATE, new OutlinerRenderState(event.getDeltaTracker()));
    }

    public static void submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, LevelRenderState levelRenderState) {
        OutlinerRenderState renderData = levelRenderState.getRenderData(OUTLINER_RENDER_STATE);
        if (renderData == null) {
            return;
        }
        getInstance().outlines.forEach(
                (_, outline) -> {
                    if (outline instanceof FineOutline) {
                        submitNodeCollector.submitCustomGeometry(
                                poseStack,
                                RenderTypes.lines(),
                                (pose, buffer) ->
                                        outline.render(pose, buffer, levelRenderState.cameraRenderState.pos, renderData.partialTick())
                        );
                    } else if (outline instanceof ThickOutline) {
                        submitNodeCollector.submitCustomGeometry(
                                poseStack,
                                getThickRenderType(),
                                (pose, buffer) ->
                                        outline.render(pose, buffer, levelRenderState.cameraRenderState.pos, renderData.partialTick())
                        );
                    }
                }
        );
//        submitNodeCollector.submitCustomGeometry(
//                poseStack,
//                RenderTypes.debugQuads(),
//                (pose, buffer) -> {
//                    getInstance().outlines.forEach((object, outlineElement) ->
//                            outlineElement.render(pose, buffer, levelRenderState.cameraRenderState.pos, renderData.partialTick())
//                    );
//                }
//        );
    }

    public static final ContextKey<OutlinerRenderState> OUTLINER_RENDER_STATE = ShakenStir.asContextKey("outliner_render_state");

    public record OutlinerRenderState(DeltaTracker partialTick) {}
}