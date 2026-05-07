package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModEnUsLangProvider extends LanguageProvider {

    public ModEnUsLangProvider(PackOutput output) {
        super(output, ShakenStir.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
//        add(ItemRegistries.BLACKBOARD.get(), "Blackboard");
//        add("itemGroup.structure_crafter", "Structure Crafter");
//        add(ItemRegistries.STRUCTURE_WAND.get(), "Structure Wand");
//        add(ItemRegistries.MATERIAL_LIST.get(), "Material List");
//        add(ItemRegistries.TELEPHONE_HANDSET.get(), "Telephone Handset");
//        add(ItemRegistries.TELEPHONE_BLOCK_ITEM.get(), "Phone Booth");
//        add(ItemRegistries.RULER_ITEM.get(), "Ruler");
        //add(BlockRegistry.TELEPHONE_BLOCK.get(), "Telephone");

        genLang(this);
    }

    public static void genLang(LanguageProvider pvd) {
        for (LangData lang : LangData.values()) {
            pvd.add(lang.key, lang.def);
        }
    }
}
