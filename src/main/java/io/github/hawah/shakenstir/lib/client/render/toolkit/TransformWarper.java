package io.github.hawah.shakenstir.lib.client.render.toolkit;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import io.github.hawah.shakenstir.lib.client.DebugGizmoRenderer;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(value = Dist.CLIENT)
public class TransformWarper {
    private static final Map<Object, TransformWarper> INSTANCES = new HashMap<>();

    public static TransformWarper instance(Object object) {
        if (!INSTANCES.containsKey(object)) {
            INSTANCES.put(object, new TransformWarper());
        }
        return INSTANCES.get(object);
    }

    private Matrix4f poseMatrix = new Matrix4f();
    private PoseStack poseStack = new PoseStack();
    private boolean active = false;
    public TransformWarper warp(PoseStack poseStack) {
        poseStack.pushPose();
        this.poseStack = poseStack;
        this.apply(poseStack);
        return this;
    }

    public TransformWarper end() {
        if (poseStack == null || poseStack.isEmpty()) {
            return this;
        }
        try {
            poseStack.popPose();
        } catch (Exception e) {
            LogUtils.getLogger().error("Error while applying pose matrix", e);
        } finally {
            poseStack = null;
        }
        return this;
    }
    public PoseStack apply(PoseStack poseStack) {
        poseStack.mulPose(poseMatrix);
        return poseStack;
    }

    public void setPoseMatrix(Matrix4f matrix4f) {
        poseMatrix.set(matrix4f);
    }
    public static boolean onKeyPressed(int button, boolean pressed) {
        LerpLike.INSTANCES.forEach((_, t) ->t.handleKeyPressed(button, pressed));
        return INSTANCES.values().stream()
                .map(transformWarper -> transformWarper.handleKeyPressed(button, pressed))
                .anyMatch(b -> b);
    }

    private boolean handleKeyPressed(int button, boolean pressed) {
        if (!pressed)
            return false;
        if (button == GLFW.GLFW_KEY_ENTER) {
            this.active = !this.active;
            if (!this.active) {
                printMatrix();
                copyMatrix();
            }
        }
        if (!this.active) {
            return false;
        }
        switch (button) {
            case GLFW.GLFW_KEY_LEFT -> {
                poseMatrix.translate(-0.1F, 0, 0);
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                poseMatrix.translate(0.1F, 0, 0);
            }
            case GLFW.GLFW_KEY_UP -> {
                poseMatrix.translate(0, 0.1F, 0);
            }
            case GLFW.GLFW_KEY_DOWN -> {
                poseMatrix.translate(0, -0.1F, 0);
            }
            case GLFW.GLFW_KEY_Z -> {
                poseMatrix.translate(0, 0, 0.1F);
            }
            case GLFW.GLFW_KEY_X -> {
                poseMatrix.translate(0, 0, -0.1F);
            }
            case GLFW.GLFW_KEY_EQUAL -> {
                poseMatrix.scale(1.1F, 1.1F, 1.1F);
            }
            case GLFW.GLFW_KEY_MINUS -> {
                poseMatrix.scale(0.9F, 0.9F, 0.9F);
            }
            case GLFW.GLFW_KEY_U -> {
                poseMatrix.rotate(0.1F, 0, 0, 1);
            }
            case GLFW.GLFW_KEY_O -> {
                poseMatrix.rotate(-0.1F, 0, 0, 1);
            }
            case GLFW.GLFW_KEY_J -> {
                poseMatrix.rotate(-0.1F, 0, 1, 0);
            }
            case GLFW.GLFW_KEY_L -> {
                poseMatrix.rotate(0.1F, 0, 1, 0);
            }
            case GLFW.GLFW_KEY_I -> {
                poseMatrix.rotate(-0.1F, 1, 0, 0);
            }
            case GLFW.GLFW_KEY_K -> {
                poseMatrix.rotate(0.1F, 1, 0, 0);
            }
            case GLFW.GLFW_KEY_R -> {
                poseMatrix.set(new Matrix4f());
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    public void render() {

    }

    public void copyMatrix() {
        copyMatrix(this.poseMatrix);
    }

    public static void copyMatrix(Matrix4f matrix4f) {
        try {
            if (!GraphicsEnvironment.isHeadless()) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(matrixToString(matrix4f)), null);
            }
        } catch (HeadlessException ignored) {
        }
    }
    public void printMatrix() {
        System.out.println(
                matrixToString()
        );
    }

    public String matrixToString() {
        return matrixToString(this.poseMatrix);
    }

    public static String matrixToString(Matrix4f matrix4f) {
        return "new Matrix4f(" +
                matrix4f.m00() + "f, " + matrix4f.m01() + "f, " + matrix4f.m02() + "f, " + matrix4f.m03() + "f, " +
                matrix4f.m10() + "f, " + matrix4f.m11() + "f, " + matrix4f.m12() + "f, " + matrix4f.m13() + "f, " +
                matrix4f.m20() + "f, " + matrix4f.m21() + "f, " + matrix4f.m22() + "f, " + matrix4f.m23() + "f, " +
                matrix4f.m30() + "f, " + matrix4f.m31() + "f, " + matrix4f.m32() + "f, " + matrix4f.m33() + "f)";
     }

     public static class LerpLike {
         private static final Map<Object, LerpLike> INSTANCES = new HashMap<>();
         float x = 0;
         float y = 0;
         float z = 0;
         float rotX = 0;
         float rotY = 0;
         float rotZ = 0;
         float scale = 1;
         private PoseStack poseStack = new PoseStack();
         private boolean active = false;

         public static LerpLike instance(Object object) {
             if (!INSTANCES.containsKey(object)) {
                 INSTANCES.put(object, new LerpLike());
             }
             return INSTANCES.get(object);
         }

         public LerpLike warp(PoseStack poseStack) {
             poseStack.pushPose();
             this.poseStack = poseStack;
             this.apply(poseStack);
             return this;
         }

         public PoseStack apply(PoseStack poseStack) {
             poseStack.translate(x, y, z);
             DebugGizmoRenderer.RotationGizmoRenderer.render(
                     poseStack,
                     Minecraft.getInstance().renderBuffers().bufferSource(),
                     1f
             );
             poseStack.mulPose(new Quaternionf(rotX, rotY, rotZ, 1));
             DebugGizmoRenderer.Translation.render(
                     poseStack,
                     Minecraft.getInstance().renderBuffers().bufferSource(),
                     1f
             );
             poseStack.scale(scale, scale, scale);
             return poseStack;
         }

         public void print() {
             System.out.println(
                     "new LerpLike(" +
                             x + "f, " + y + "f, " + z + "f, " +
                             rotX + "f, " + rotY + "f, " + rotZ + "f, " +
                             scale + "f)"
             );
         }

         private boolean handleKeyPressed(int button, boolean pressed) {
             if (!pressed)
                 return false;
             if (button == GLFW.GLFW_KEY_ENTER) {
                 this.active = !this.active;
                 if (!this.active) {
                     print();
                 }
             }
             if (!this.active) {
                 return false;
             }
             switch (button) {
                 case GLFW.GLFW_KEY_LEFT -> {
                     x -= 0.1F;
                 }
                 case GLFW.GLFW_KEY_RIGHT -> {
                     x += 0.1F;
                 }
                 case GLFW.GLFW_KEY_UP -> {
                     y += 0.1F;
                 }
                 case GLFW.GLFW_KEY_DOWN -> {
                     y -= 0.1F;
                 }
                 case GLFW.GLFW_KEY_Z -> {
                     z -= 0.1F;
                 }
                 case GLFW.GLFW_KEY_X -> {
                     z += 0.1F;
                 }
                 case GLFW.GLFW_KEY_EQUAL -> {
                     scale *= 1.1F;
                 }
                 case GLFW.GLFW_KEY_MINUS -> {
                     scale /= 1.1F;
                 }
                 case GLFW.GLFW_KEY_U -> {
                     rotX += 0.1F;
                 }
                 case GLFW.GLFW_KEY_O -> {
                     rotX -= 0.1F;
                 }
                 case GLFW.GLFW_KEY_J -> {
                     rotZ -= 0.1F;
                 }
                 case GLFW.GLFW_KEY_L -> {
                     rotZ += 0.1F;
                 }
                 case GLFW.GLFW_KEY_I -> {
                     rotY += 0.1F;
                 }
                 case GLFW.GLFW_KEY_K -> {
                     rotY -= 0.1F;
                 }
                 case GLFW.GLFW_KEY_R -> {
                     x = 0;
                     y = 0;
                     z = 0;
                     rotX = 0;
                     rotY = 0;
                     rotZ = 0;
                     scale = 1;
                 }
                 default -> {
                     return false;
                 }
             }
             return true;
         }

         public LerpLike end() {
             if (poseStack == null || poseStack.isEmpty()) {
                 return this;
             }
             try {
                 poseStack.popPose();
             } catch (Exception e) {
                 LogUtils.getLogger().error("Error while applying pose matrix", e);
             } finally {
                 poseStack = null;
             }
             return this;
         }
     }

    @SubscribeEvent
    public static void onKeyPressed(InputEvent.Key event) {
        if (event.getAction() != InputConstants.PRESS) {
            return;
        }
        int key = event.getKey();

        onKeyPressed(key, true);
    }

}
