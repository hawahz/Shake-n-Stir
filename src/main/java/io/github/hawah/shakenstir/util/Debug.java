package io.github.hawah.shakenstir.util;

import io.github.hawah.shakenstir.Config;
import io.github.hawah.shakenstir.foundation.networking.ClientboundDebugBlockDisplayPacket;
import io.github.hawah.shakenstir.lib.client.render.outliner.Outliner;
import io.github.hawah.shakenstir.lib.networking.Networking;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class Debug {

    public static void drawBlock(BlockPos blockPos, Level level) {
        if (Config.Common.DEBUG_MODE.get()) {
            if (level.isClientSide()){
                Networking.sendToAll(new ClientboundDebugBlockDisplayPacket(blockPos, blockPos));
            } else {
                Outliner.getInstance()
                        .chaseBox(new Object(), blockPos, blockPos)
                        .setRGBA(0, 1, 1, 1)
                        .lazyDiscard(200)
                        .finish();
            };
        }
    }
}
