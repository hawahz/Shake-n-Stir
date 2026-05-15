package io.github.hawah.shakenstir.util;

import io.github.hawah.shakenstir.lib.client.KeyBinding;

public class ShakeClientHooks {
    public static void shake() {

    }

    public static boolean hasControlDown() {
        return KeyBinding.hasControlDown();
    }
}
