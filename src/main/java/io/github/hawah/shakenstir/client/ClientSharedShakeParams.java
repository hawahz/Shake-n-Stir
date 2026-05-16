package io.github.hawah.shakenstir.client;

import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import net.minecraft.util.Mth;

import java.util.HashMap;

public class ClientSharedShakeParams {
    static HashMap<Integer, Param> clientIdToParam = new HashMap<>();

    public static Param getParam(int id) {
        if (!clientIdToParam.containsKey(id)) {
            clientIdToParam.put(id, new Param());
        }
        return clientIdToParam.get(id);
    }

    public static double x(int id) {
        Param param = getParam(id);
        double pastTime = AnimationTickHolder.getRenderTime() - param.lastTick;
        double delta = Mth.clamp(pastTime / param.tickLen, 0, 1);
        return Mth.lerp(delta, param.ox, param.x);
    }

    public static double y(int id) {
        Param param = getParam(id);
        double pastTime = AnimationTickHolder.getRenderTime() - param.lastTick;
        double delta = Mth.clamp(pastTime / param.tickLen, 0, 1);
        return Mth.lerp(delta, param.oy, param.y);
    }

    public static void updateParam(int id, double x, double y) {
        Param param = getParam(id);
        param.ox = param.x;
        param.x = x;
        param.oy = param.y;
        param.y = y;
        param.tickLen = AnimationTickHolder.getRenderTime() - param.lastTick;
        param.lastTick = AnimationTickHolder.getRenderTime();
    }

    public static class Param {
        public double x = 0, ox = 0;
        public double y = 0, oy = 0;
        public double lastTick = 0;
        public double tickLen = 0;
    }
}
