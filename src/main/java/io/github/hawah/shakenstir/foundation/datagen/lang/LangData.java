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

    TOOLTIP_MASTERPIECE ("tooltip_masterpiece", "Masterpiece", 0, ChatFormatting.ITALIC, ChatFormatting.DARK_AQUA),
    TOOLTIP_SUPERIOR    ("tooltip_superior", "Superior", 0, ChatFormatting.ITALIC, ChatFormatting.DARK_AQUA),
    TOOLTIP_EXCELLENT   ("tooltip_excellent", "Excellent", 0, ChatFormatting.ITALIC, ChatFormatting.DARK_AQUA),
    TOOLTIP_GOOD        ("tooltip_good", "Good", 0, ChatFormatting.ITALIC, ChatFormatting.DARK_AQUA),
    TOOLTIP_AVERAGE     ("tooltip_average", "Average", 0, ChatFormatting.ITALIC, ChatFormatting.DARK_AQUA),
    TOOLTIP_POOR        ("tooltip_poor", "Poor", 0, ChatFormatting.ITALIC, ChatFormatting.DARK_AQUA),
    TOOLTIP_BAD         ("tooltip_bad", "Bad", 0, ChatFormatting.ITALIC, ChatFormatting.DARK_AQUA),
    TOOLTIP_TERRIBLE    ("tooltip_terrible", "Terrible", 0, ChatFormatting.ITALIC, ChatFormatting.DARK_AQUA),
    TOOLTIP_DISASTER    ("tooltip_disaster", "Disaster", 0, ChatFormatting.ITALIC, ChatFormatting.DARK_AQUA),

    TOOLTIP_TITLE_COCKTAIL("tooltip_title_cocktail", "Cocktail: %s", 1),
    TOOLTIP_TITLE_BASE  ("tooltip_title_base", "Base: %s", 1),
    TOOLTIP_TITLE_ICE_LEVEL("tooltip_title_ice_level", "Ice Level: %s", 1),
    TOOLTIP_TITLE_DRUNK_LEVEL("tooltip_title_drunk_level", "Alcohol: %s°", 1),

    TOOLTIP_SHAKER_FLUID("tooltip_shaker_fluid", "%s %s mb", 2),
    TOOLTIP_DISTILLER_CONTENT("tooltip_distiller_content", "Content:", 0, ChatFormatting.BOLD),
    TOOLTIP_DISTILLER_BURNING_TIME("tooltip_distiller_burning_time", "Burning Time: %ss", 1, ChatFormatting.GRAY),

    TOOLTIP_WHEN_SPIRIT_AS_BASE("tooltip_when_spirit_as_base", "When Applied as Base:", 0, ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
    TOOLTIP_SPIRIT_POSITIVE_N_NEGATIVE("tooltip_spirit_positive_negative", "%s or %s", 2),
    TOOLTIP_SCROLL_SHAKER_SHAKE_TIME("tooltip_scroll_shaker_shake_time", "Shakes %s times", 1),
    TOOLTIP_SCROLL_RECIPE_REQUIRED("tooltip_scroll_recipe_required", "Required:", 0, ChatFormatting.BLUE),
    TOOLTIP_SCROLL_RECIPE_RESULT("tooltip_scroll_recipe_result", "Product: %s", 1),
    TOOLTIP_SCROLL_RECIPE_SHAKE_TIMES("tooltip_scroll_recipe_shake_times", "Shake %s times", 1),

    TOOLTIP_MINT_SIZE_SMALL("tooltip_mint_size_small", "Small", 0, ChatFormatting.DARK_GREEN),
    TOOLTIP_MINT_SIZE_MEDIUM("tooltip_mint_size_medium", "Medium", 0, ChatFormatting.DARK_GREEN),
    TOOLTIP_MINT_SIZE_LARGE("tooltip_mint_size_large", "Large", 0, ChatFormatting.DARK_GREEN),


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

    NAME_SOUR       ("name.sour",       "%s %s Sour"    , 0),
    NAME_COCKTAIL   ("name.cocktail",   "%s %s Cocktail", 0),
    NAME_COLADA     ("name.colada",     "%s %s Colada"  , 0),
    NAME_FIZZ       ("name.fizz",       "%s %s Fizz"    , 0),
    NAME_HIGHBALL   ("name.highball",   "%s Highball"   , 0),
    NAME_TONIC      ("name.tonic",      "%s %s Tonic"   , 0),
    NAME_SUSPICIOUS ("name.suspicious",      "Suspicious %s2 %s1"   , 0),
    NAME_MARGARITA  ("name.margarita",  "Margarita"     , 0),
    NAME_MOJITO  ("name.mojito",  "Mojito"     , 0),
    NAME_LONG_ISLAND_ICED_TEA  ("name.long_island_iced_tea",  "Long Island Iced Tea"     , 0),

    ITEM_NAME_MARTINI_GLASS("name.martini_glass", "Martini Glass", 0),
    ITEM_NAME_COLLINS_GLASS("name.collins_glass", "Collins Glass", 0),
    ITEM_NAME_MARGARITA_GLASS("name.margarita_glass", "Margarita Glass", 0),

    CONFIGURATION_ENABLE_WRONG_FLUID_IN_BOTTLE("configuration.enable_wrong_fluid_in_bottle", "Enable Wrong Fluid in Bottle", 0),
    CONFIGURATION_ENABLE_WRONG_FLUID_IN_BOTTLE_TOOLTIP("configuration.enable_wrong_fluid_in_bottle.tooltip", "", 0),

    CONFIGURATION_SHAKER_ANIMATION_UPLOAD_DISTANCE("configuration.shaker_animation_upload_distance", "Shaker Animation Upload Distance", 0),
    CONFIGURATION_SHAKER_ANIMATION_UPLOAD_DISTANCE_TOOLTIP("configuration.shaker_animation_upload_distance.tooltip", "The distance between the shaker and the player, beyond which the shaker will not animate.", 0),

    ADVANCEMENT_SHAKE_ROOT_TITLE("advancements.shakenstir.shake.root.title", "Shaken, not stirred.", 0),
    ADVANCEMENT_SHAKE_ROOT_DESC("advancements.shakenstir.shake.root.description", "Gained Shaker", 0),
    ADVANCEMENT_SHAKE_BUBBLE_TITLE("advancements.shakenstir.shake.bubble.title", "WTF!", 0),
    ADVANCEMENT_SHAKE_BUBBLE_DESC("advancements.shakenstir.shake.bubble.description", "Shook sparkling water. Regretted it.", 0),
    ADVANCEMENT_SHAKE_SHAKER_OVERTURN_TITLE("advancements.shakenstir.shake.shaker_overturn.title", "Watch out!", 0),
    ADVANCEMENT_SHAKE_SHAKER_OVERTURN_DESC("advancements.shakenstir.shake.shaker_overturn.description", "Knock over a shaker.", 0),

    ADVANCEMENT_DRINK_ROOT_TITLE("advancements.shakenstir.drink.root.title", "Hold on. Let me drink.", 0),
    ADVANCEMENT_DRINK_ROOT_DESC("advancements.shakenstir.drink.root.description", "Gained Spirit Bottle", 0),
    ADVANCEMENT_DRINK_FIRST_TITLE("advancements.shakenstir.drink.first.title", "Drank first. Thought later.", 0),
    ADVANCEMENT_DRINK_FIRST_DESC("advancements.shakenstir.drink.first.description", "Got drunk.", 0),

    ADVANCEMENT_DRINK_HEAVY_TITLE("advancements.shakenstir.drink.drunk_heavy.title", "Heavyweight", 0),
    ADVANCEMENT_DRINK_HEAVY_DESC("advancements.shakenstir.drink.drunk_heavy.description", "Get heavily drunk.", 0),

    ADVANCEMENT_DRINK_FIRST_FALL_TITLE("advancements.shakenstir.drink.first_fall_by_drunk.title", "Drunken Misstep", 0),
    ADVANCEMENT_DRINK_FIRST_FALL_DESC("advancements.shakenstir.drink.first_fall_by_drunk.description", "Fall while under the influence.", 0),

    ADVANCEMENT_DRINK_LEMON_HIT_TITLE("advancements.shakenstir.drink.first_hit_due_to_lemon.title", "When Life Gives You Lemons...", 0),
    ADVANCEMENT_DRINK_LEMON_HIT_DESC("advancements.shakenstir.drink.first_hit_due_to_lemon.description", "Attack after tasting lemon.", 0),

    ADVANCEMENT_DRINK_PARALYSIS_DEATH_TITLE("advancements.shakenstir.drink.died_by_discovering_paralysis.title", "A Fatal Discovery", 0),
    ADVANCEMENT_DRINK_PARALYSIS_DEATH_DESC("advancements.shakenstir.drink.died_by_discovering_paralysis.description", "Die from paralysis damage.", 0),

    ADVANCEMENT_DRINK_PARALYSIS_PROTECT_TITLE("advancements.shakenstir.drink.protected_by_paralysis.title", "Saved by the Freeze", 0),
    ADVANCEMENT_DRINK_PARALYSIS_PROTECT_DESC("advancements.shakenstir.drink.protected_by_paralysis.description", "Survive lethal damage thanks to paralysis.", 0),

    // ======================== Dialogue Editor GUI ========================
    GUI_DIALOGUE_EDITOR_TITLE("gui.dialogue_editor.title", "Dialogue Editor", 0),
    GUI_DIALOGUE_EDITOR_BTN_SAVE("gui.dialogue_editor.btn.save", "Save", 0),
    GUI_DIALOGUE_EDITOR_BTN_COPY("gui.dialogue_editor.btn.copy", "Copy", 0),
    GUI_DIALOGUE_EDITOR_BTN_PASTE("gui.dialogue_editor.btn.paste", "Paste", 0),
    GUI_DIALOGUE_EDITOR_BTN_ADD_ENTRY("gui.dialogue_editor.btn.add_entry", "+ Entry", 0),
    GUI_DIALOGUE_EDITOR_BTN_DEL_ENTRY("gui.dialogue_editor.btn.del_entry", "- Entry", 0),
    GUI_DIALOGUE_EDITOR_BTN_ADD_COND("gui.dialogue_editor.btn.add_cond", "+Cond", 0),
    GUI_DIALOGUE_EDITOR_BTN_DEL_COND("gui.dialogue_editor.btn.del_cond", "-Cond", 0),
    GUI_DIALOGUE_EDITOR_BTN_ADD_TEXT("gui.dialogue_editor.btn.add_text", "+Text", 0),
    GUI_DIALOGUE_EDITOR_BTN_DEL_TEXT("gui.dialogue_editor.btn.del_text", "-Text", 0),
    GUI_DIALOGUE_EDITOR_BTN_APPLY("gui.dialogue_editor.btn.apply", "Apply", 0),
    GUI_DIALOGUE_EDITOR_BTN_SET_FREQ("gui.dialogue_editor.btn.set_freq", "SetFreq", 0),
    GUI_DIALOGUE_EDITOR_BTN_HELP("gui.dialogue_editor.btn.help", "[?]", 0),
    GUI_DIALOGUE_EDITOR_FREQ("gui.dialogue_editor.freq", "freq", 0),
    GUI_DIALOGUE_EDITOR_COND_VAL("gui.dialogue_editor.cond_val", "val", 0),
    GUI_DIALOGUE_EDITOR_TEXT("gui.dialogue_editor.text", "text", 0),
    GUI_DIALOGUE_EDITOR_COND_TYPE("gui.dialogue_editor.cond_type", "Type: %s", 1),
    GUI_DIALOGUE_EDITOR_COND_OP("gui.dialogue_editor.cond_op", "Op: %s", 1),
    GUI_DIALOGUE_EDITOR_COND_TYPE_UNKNOWN("gui.dialogue_editor.cond_type_unknown", "Type: ?", 0),
    GUI_DIALOGUE_EDITOR_ENTRY("gui.dialogue_editor.entry", "Entry #%s", 1),
    GUI_DIALOGUE_EDITOR_NO_ENTRY("gui.dialogue_editor.no_entry", "No entry selected", 0),
    GUI_DIALOGUE_EDITOR_LOADING("gui.dialogue_editor.loading", "Loading...", 0),
    GUI_DIALOGUE_EDITOR_MSG_COPIED("gui.dialogue_editor.msg.copied", "Copied %s dialogue entries", 1),
    GUI_DIALOGUE_EDITOR_MSG_PASTED("gui.dialogue_editor.msg.pasted", "Pasted %s dialogue entries", 1),
    GUI_DIALOGUE_EDITOR_MSG_CLIPBOARD_EMPTY("gui.dialogue_editor.msg.clipboard_empty", "Clipboard is empty!", 0),
    GUI_DIALOGUE_EDITOR_MSG_UNSAVED("gui.dialogue_editor.msg.unsaved", "Unsaved changes. Use 'Save' to apply.", 0),

    // ======================== Placeholder Help ========================
    GUI_DIALOGUE_HELP_TITLE("gui.dialogue_editor.help.title", "Placeholders", 0),
    GUI_DIALOGUE_HELP_PLAYER_NAME("gui.dialogue_editor.help.player_name", "{player_name} - current interacting player name", 0),
    GUI_DIALOGUE_HELP_RECIPE_NAME("gui.dialogue_editor.help.recipe_name", "{recipe_name} - current recipe display name", 0),
    GUI_DIALOGUE_HELP_SEARCH_ITEM("gui.dialogue_editor.help.search_item_name", "{search_item_name} - item being searched for", 0),
    GUI_DIALOGUE_HELP_SEARCH_TICKS("gui.dialogue_editor.help.search_elapsed_ticks", "{search_elapsed_ticks} - ticks spent searching", 0),
    GUI_DIALOGUE_HELP_ACTIVITY("gui.dialogue_editor.help.current_activity", "{current_activity} - current AI activity name", 0),
    GUI_DIALOGUE_HELP_BR("gui.dialogue_editor.help.br_marker", "[BR] - splits text into multiple speech bubbles", 0),

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

    public static final Component ERROR = Component.literal("Error").withStyle(ChatFormatting.DARK_RED).withStyle(ChatFormatting.ITALIC);

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
