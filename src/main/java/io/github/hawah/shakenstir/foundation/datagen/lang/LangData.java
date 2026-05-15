package io.github.hawah.shakenstir.foundation.datagen.lang;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("unused")
public enum LangData {
    SHIFT("tooltip_shift", "-[Shift]-", 0, ChatFormatting.DARK_GRAY , ChatFormatting.ITALIC),
    TOOLTIP_SPIRIT_CONTENT("tooltip_spirit_content", "Content: %s", 1),
    TOOLTIP_SPIRIT_EMPTY("tooltip_spirit_empty", "Empty", 0),
    TOOLTIP_SPIRIT_VOLUME("tooltip_spirit_volumn", "Volume: %s mb", 1),
    TOOLTIP_SHAKE_SHAKING("tooltip_shake_shaking", "Shaking...", 0, ChatFormatting.ITALIC, ChatFormatting.DARK_AQUA),
    TOOLTIP_SHAKE_CONTENT("tooltip_shake_content", "Ingredients:", 0),
    TOOLTIP_SHAKE_FLUID_CONTENT("tooltip_shake_fluid_content", "Base:", 0),

    HUD_TIP_BLACKBOARD_SELECT_FIRST_POINT("hud.blackboard_select_first_point", "Select the first point", 0),
    HUD_TIP_BLACKBOARD_SELECT_SECOND_POINT("hud.blackboard_select_second_point","Select the second point", 0),
    HUD_TIP_BLACKBOARD_CLEAR_AND_SELECT_FIRST("hud.blackboard_clear_and_select_first","Clear and Select First Point", 0),
    HUD_TIP_BLACKBOARD_SELECT_ANCHOR("hud.blackboard_select_anchor","Set Anchor", 0),
    HUD_TIP_BLACKBOARD_DELETE_ALL("hud.blackboard_delete_all","Delete All", 0),
    HUD_TIP_BLACKBOARD_DELETE_ANCHOR("hud.blackboard_delete_anchor","Delete Anchor", 0),
    HUD_TIP_BLACKBOARD_SHOW_ALL_FACES("hud.blackboard_show_all_faces","Show All Faces", 0),
    HUD_TIP_BLACKBOARD_PICK_AIR_CENTER("hud.blackboard_pick_air_center","Pick Air Center", 0),
    HUD_TIP_BLACKBOARD_PICK_AIR_POINT("hud.blackboard_pick_air_point","Pick Air Point", 0),
    HUD_TIP_BLACKBOARD_CHANGE_DISTANCE("hud.blackboard_change_distance","Change Reach Distance", 0),
    HUD_TIP_BLACKBOARD_SELECT_OPPOSITE_FACE("hud.blackboard_select_opposite_face","Select Opposite Face", 0),
    HUD_TIP_BLACKBOARD_PUSH_OR_PULL_FACE("hud.blackboard_push_or_pull_face","Push/Pull Face", 0),

    HUD_BLACKBOARD_SELECTION("hud.blackboard_selection", "Size (%1$s, %2$s, %3$s) (%4$s)", 4),

    NAME_SOUR("name.sour", "%s %s Sour", 0),
    NAME_COCKTAIL("name.cocktail", "%s %s Cocktail", 0),

    ITEM_NAME_MARTINI_GLASS("name.martini_glass", "Martini Glass", 0),
    ITEM_NAME_COLLINS_GLASS("name.collins_glass", "Collins Glass", 0),
    ITEM_NAME_MARGARITA_GLASS("name.margarita_glass", "Margarita Glass", 0),
    ;

    public final String key;
    public final String def;
    private final int arg;
    private final ChatFormatting[] format;

    LangData(String key, String def, int arg, @Nullable ChatFormatting... format) {
        this.key = ShakenStir.MODID + "." + key;
        this.def = def;
        this.arg = arg;
        this.format = format;
    }

    private static List<LangData> getTitleLang(){
        return List.of(LangData.values());
    }

    public MutableComponent get(Object... args) {
        if (args.length != arg) {
            throw new IllegalArgumentException("for " + name() + ": expect " + arg + " parameters, got " + args.length);
        }
        MutableComponent ans = Component.translatable(key, args);
        if (format != null) {
            return ans.withStyle(format);
        }
        return ans;
    }

    public static MutableComponent getFromTag(String tag) {
        List<LangData> titleLang = getTitleLang();
        for (LangData data : titleLang){
            if (data.key.equals(ShakenStir.MODID + ".tooltip."+tag)){
                MutableComponent ans = Component.translatable(data.key);
                if (data.format != null) {
                    return ans.withStyle(data.format);
                }
                return ans;
            }
        }
        return Component.literal("Error").withStyle(ChatFormatting.DARK_RED).withStyle(ChatFormatting.ITALIC);
    }

    public static MutableComponent getFromItem(String tag) {
        List<LangData> titleLang = getTitleLang();
        for (LangData data : titleLang){
            if (data.key.equals(ShakenStir.MODID + ".name."+tag)){
                MutableComponent ans = Component.translatable(data.key);
                if (data.format != null) {
                    return ans.withStyle(data.format);
                }
                return ans;
            }
        }
        return Component.literal("Error").withStyle(ChatFormatting.DARK_RED).withStyle(ChatFormatting.ITALIC);
    }

}
