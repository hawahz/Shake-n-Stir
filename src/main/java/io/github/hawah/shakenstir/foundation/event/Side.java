package io.github.hawah.shakenstir.foundation.event;

import net.neoforged.api.distmarker.Dist;

/**
 * 事件所在端的枚举，用于区分事件应在哪一端注册和派发。
 * <p>
 * 事件系统通过 {@code FMLEnvironment.dist} 获取当前运行时的物理端，
 * 自动过滤不匹配端的事件类型和订阅者。
 *
 * <h3>各端说明</h3>
 * <ul>
 *   <li>{@link #COMMON} — 两端通用，始终有效</li>
 *   <li>{@link #CLIENT} — 仅物理客户端有效，服务端会被静默忽略</li>
 *   <li>{@link #SERVER} — 仅物理服务端（专用服务器）有效，客户端会被静默忽略</li>
 * </ul>
 *
 * @see SnsRegisterEvent#value()
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public enum Side {
    COMMON,
    CLIENT,
    SERVER;

    private static final Dist PHYSICAL_DIST;

    static {
        Dist dist = Dist.CLIENT; // 安全默认值
        try {
            // 方案1: NeoForge FMLEnvironment.dist（静态字段）
            Class<?> fmlEnvClass = Class.forName("net.neoforged.fml.loading.FMLEnvironment");
            dist = (Dist) fmlEnvClass.getField("dist").get(null);
        } catch (Exception e1) {
            try {
                // 方案2: FML 环境中的 FMLLoader（反射获取实例）
                Class<?> fmlLoaderClass = Class.forName("net.neoforged.fml.loading.FMLLoader");
                java.lang.reflect.Method m = fmlLoaderClass.getMethod("getDist");
                dist = (Dist) m.invoke(null);
            } catch (Exception e2) {
                // 极端情况下无法获取，保持默认值（客户端）
            }
        }
        PHYSICAL_DIST = dist;
    }

    /**
     * 获取当前运行时的物理端。
     */
    public static Dist getPhysicalDist() {
        return PHYSICAL_DIST;
    }

    /**
     * 判断当前物理端是否是客户端。
     */
    public static boolean isClient() {
        return PHYSICAL_DIST.isClient();
    }

    /**
     * 判断当前物理端是否是专用服务器。
     */
    public static boolean isServer() {
        return PHYSICAL_DIST.isDedicatedServer();
    }

    /**
     * 检查此事件端是否与当前物理端兼容。
     * <p>
     * {@link #COMMON} 始终兼容；{@link #CLIENT} 仅在物理客户端兼容；
     * {@link #SERVER} 仅在物理服务端兼容。
     *
     * @return {@code true} 如果该端的事件可以在当前运行时注册
     */
    public boolean isValidForCurrentSide() {
        return switch (this) {
            case COMMON -> true;
            case CLIENT -> PHYSICAL_DIST.isClient();
            case SERVER -> PHYSICAL_DIST.isDedicatedServer();
        };
    }
}
