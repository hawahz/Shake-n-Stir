package io.github.hawah.shakenstir;

import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {

    public static class Common {
        public static final ModConfigSpec SPEC;
        public static final ModConfigSpec.BooleanValue DEBUG_MODE;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            DEBUG_MODE = builder
                    .define("debug_mode", false);

            SPEC = builder.build();
        }
    }

    public static class Server {
        public static final ModConfigSpec SPEC;

        public static final ModConfigSpec.BooleanValue ENABLE_WRONG_FLUID_IN_BOTTLE;
        public static final ModConfigSpec.IntValue SHAKER_ANIMATION_UPLOAD_DISTANCE;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            ENABLE_WRONG_FLUID_IN_BOTTLE = builder
                    .comment(LangData.CONFIGURATION_ENABLE_WRONG_FLUID_IN_BOTTLE.def)
                    .translation(LangData.CONFIGURATION_ENABLE_WRONG_FLUID_IN_BOTTLE.key)
                    .define("enable_wrong_fluid_in_bottle", true);

            SHAKER_ANIMATION_UPLOAD_DISTANCE = builder
                    .comment(LangData.CONFIGURATION_SHAKER_ANIMATION_UPLOAD_DISTANCE.def)
                    .translation(LangData.CONFIGURATION_SHAKER_ANIMATION_UPLOAD_DISTANCE.key)
                    .defineInRange("shaker_animation_upload_distance", 256, 0, 4096);

            SPEC = builder.build();
        }
    }

//    private static boolean validateItemName(final Object obj) {
//        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(Identifier.parse(itemName));
//    }
}
