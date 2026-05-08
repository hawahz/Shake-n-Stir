package io.github.hawah.shakenstir.client.gui;

import io.github.hawah.shakenstir.lib.client.gui.BaseScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Vector4i;

import java.awt.*;
import java.util.Arrays;
import java.util.Random;

/**
 * 2D 液面物理模拟（固定物理步长，可变渲染插值）
 *
 * 用法：
 *   water.tick();                 // 每秒调用 20 次，固定 0.05s 步长
 *   water.render(partialTick);    // 每渲染帧调用，partialTick ∈ [0,1)
 *
 * 内部基于一维波动方程，左边界追踪目标高度产生自然扰动。
 */
public class FluidSim {

    /* ========== 物理参数（已适配 dt = 1/20 s） ========== */
    private static final int N = 10;                 // 离散点数
    private static final double DX = 1.0;             // 空间步长
    private static final double C = 4.0;              // 波速
    private static final double DT = 1.0 / 5.0;      // 固定时间步长 (0.05s)
    private static final double DAMP = 0.01;         // 速度阻尼（已针对 0.05s 步长微调）
    private static final double MAX_STEP = 0.75;       // 左边界每 tick 最大移动量（原 0.6/帧 → 考虑 dt 比例调整）

    /* 绘图坐标（像素） */
    private int waterBaseY;
    private int bottomY;
    private int leftWallX;
    private int rightWallX;

    /* 物理状态数组 */
    private double[] h = new double[N];           // 当前物理状态（最新 tick 后）
    private double[] hPrev = new double[N];       // 前一物理状态（用于波动方程加速度）
    private double[] hNew = new double[N];        // 计算缓冲区

    /* 插值用的“上一 tick 状态”副本 */
    private double[] hPrevRender = new double[N]; // 在每次 tick() 开始前保存，与 h 一起构成插值对

    /* 左边界控制 */
    private double targetHeight = 0;              // 目标液面高度
    private double leftHeight = 0;                // 当前左边界物理值（最新 tick 后）
    private double leftHeightPrevRender = 0;      // 用于插值的左边界上一 tick 值

    public FluidSim(int waterBaseY, int bottomY, int leftWallX, int rightWallX) {
        this.waterBaseY = waterBaseY;
        this.bottomY = bottomY;
        this.leftWallX = leftWallX;
        this.rightWallX = rightWallX;
    }

    public void setWaterBaseY(int waterBaseY) {
        this.waterBaseY = waterBaseY;
    }

    public void setBottomY(int bottomY) {
        this.bottomY = bottomY;
    }

    public void setLeftWallX(int leftWallX) {
        this.leftWallX = leftWallX;
    }

    public void setRightWallX(int rightWallX) {
        this.rightWallX = rightWallX;
    }

    /**
     * 设置目标液面高度，变化时会通过左边界移动产生连续波浪。
     */
    public void setTargetHeight(double target) {
        this.targetHeight = target;
    }

    public void forceSetHeight(double height) {
        for (int i = 0; i < h.length; i++) {
            h[i] = height;
            hPrev[i] = height;
            hPrevRender[i] = height;
        }
        leftHeight = height;
        leftHeightPrevRender = height;
    }

    /**
     * 物理更新一步，必须以精确的 1/20 秒间隔调用。
     * 内部固定时间步长 DT = 0.05s。
     */
    public void tick() {
        // ---- 保存上一 tick 状态，用于渲染插值 ----
        System.arraycopy(h, 0, hPrevRender, 0, N);
        leftHeightPrevRender = leftHeight;

        // 1. 左边界移动，叠加微小随机漂移，让运动不那么机械
        double diff = targetHeight - leftHeight;
        if (Math.abs(diff) > MAX_STEP) {
            leftHeight += Math.signum(diff) * MAX_STEP;
        } else {
            leftHeight = targetHeight;
        }
        // 加入 ±0.15 范围的随机抖动（幅度可根据美术感觉调整）
        leftHeight += (Math.random() - 0.5) * 0.1;

        // 2. 目标高度变化时，在左边界附近施加一次随机脉冲（模拟注水/抽水溅射）
        if (Math.abs(diff) > 0.01) {
            double impulse = diff * 0.6;          // 变化越大，脉冲越强
            for (int i = 1; i <= 3 && i < N - 1; i++) {
                // 对 hPrev 动手脚相当于改变了速度，下一帧就会产生波浪
                hPrev[i] += impulse * (Math.random() - 0.5) * (1.0 - i * 0.25);
            }
        }

        // 3. 波动方程更新内部点，增大随机力幅度
        double coeff = (C * DT / DX) * (C * DT / DX);
        for (int i = 1; i < N - 1; i++) {
            double accel = coeff * (h[i + 1] - 2.0 * h[i] + h[i - 1]);
            // 基础随机力 ±0.03，增加微小的持续扰动
            hNew[i] = 2.0 * h[i] - hPrev[i] + accel;
            hNew[i] -= DAMP * (hNew[i] - h[i]);
        }

        // 4. 边界处理（保持原有设计：左右均为左边界高度）
        hNew[0] = leftHeight;
        hNew[N - 1] = leftHeight;

        // 5. 数组轮转
        double[] temp = hPrev;
        hPrev = h;
        h = hNew;
        hNew = temp;
        Arrays.fill(hNew, 0);
    }

    /**
     * 渲染液面，在两次 tick 之间平滑插值。
     *
     * @param partialTick 当前帧相对上一 tick 的时间偏移，范围 [0, 1)
     *                    0 表示刚好在上一 tick 完成时渲染，
     *                    0.9 表示距离下一 tick 还有 10% 的时间。
     */
    public void render(GuiGraphicsExtractor guiGraphics, float partialTick, float alpha) {
        DrawFunction.warp(guiGraphics);

        // 插值系数（确保范围安全）
        float t = Math.max(0.0f, Math.min(partialTick, 1.0f));

        // 水体竖线纹理（对液面进行插值）
        DrawFunction.setColor(new Color(58, 124, 165, (int) Mth.clamp(100 * alpha, 0, 255)));
        double dxScreen = (double) (rightWallX - leftWallX) / (N - 1);
        // 逐像素绘制水体竖线，不再依赖物理采样点数N
        for (int x = leftWallX; x <= rightWallX; x++) {
            // 当前像素在物理数组中的浮点索引
            double index = (double) (x - leftWallX) * (N - 1) / (rightWallX - leftWallX);
            int i0 = (int) index;
            double frac = index - i0;
            if (i0 >= N - 1) {
                i0 = N - 1;
                frac = 0;
            }

            int i0Next = Math.min(N - 1, i0 + 1);

            // 上一物理状态在 index 处的插值高度
            double hiPrev = hPrevRender[i0] + frac * (hPrevRender[i0Next] - hPrevRender[i0]);
            // 当前物理状态在 index 处的插值高度
            double hiCurr = h[i0] + frac * (h[i0Next] - h[i0]);
            // 用 partialTick 混合两个物理状态
            double hi = hiPrev + t * (hiCurr - hiPrev);

            int yTop = waterBaseY - (int) hi;
            DrawFunction.line(new Point(x, yTop), new Point(x, bottomY));
        }

        // 液面轮廓线
        DrawFunction.setColor(new Color(160, 216, 239, (int)Mth.clamp(255 * alpha, 0, 255)));
        for (int i = 0; i < N - 1; i++) {
            double hi0 = hPrevRender[i]     + t * (h[i]     - hPrevRender[i]);
            double hi1 = hPrevRender[i + 1] + t * (h[i + 1] - hPrevRender[i + 1]);
            int x0 = leftWallX + (int) (i * dxScreen);
            int y0 = waterBaseY - (int) hi0;
            int x1 = leftWallX + (int) ((i + 1) * dxScreen);
            int y1 = waterBaseY - (int) hi1;
            DrawFunction.line(new Point(x0, y0), new Point(x1, y1));
        }
    }
    
    static class DrawFunction {
        static Vector4i color = new Vector4i(255, 255, 255, 255);
        static GuiGraphicsExtractor graphicsExtractor;

        public static void warp(GuiGraphicsExtractor graphics) {
            graphicsExtractor = graphics;
        }
        
        public static void setColor(Color c) {
            color.set(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        }
        
        public static void line(Point p0, Point p1) {
            BaseScreen.line(
                    graphicsExtractor,
                    p0.x,
                    p0.y,
                    p1.x,
                    p1.y,
                    ARGB.color(color.w, color.x, color.y, color.z)
            );
        }
        
        public static void fill(Point p0, Point p1) {
            graphicsExtractor.fill(
                    p0.x,
                    p0.y,
                    p1.x,
                    p1.y,
                    ARGB.color(color.w, color.x, color.y, color.z)
            );
        }
    }
}