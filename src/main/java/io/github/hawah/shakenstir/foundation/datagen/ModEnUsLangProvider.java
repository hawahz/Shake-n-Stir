package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import io.github.hawah.shakenstir.content.fluid.FluidTypeRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import static io.github.hawah.shakenstir.ShakenStir.MODID;

public class ModEnUsLangProvider extends LanguageProvider {

    public ModEnUsLangProvider(PackOutput output) {
        super(output, MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(ItemRegistries.GIN.get(), "Gin");
        add(ItemRegistries.SHAKE.get(), "Shake");
        add(ItemRegistries.SHAKE_CUP.get(), "Shake Cup");
        add("itemGroup.shakenstir", "Shake n Stir");
        add("itemGroup." + MODID + ".tab", "Shake n Stir");
        add(ItemRegistries.ICE_CUBE.get(), "Ice Cube");
//        add(FluidRegistries.GIN_SOURCE_FLUID_BLOCK.getRegisteredName(), "");
        add(FluidTypeRegistries.GIN_FLUID_TYPE.get().getDescriptionId(), "Gin");
        genLang(this);
    }

    public static void genLang(LanguageProvider pvd) {
        for (LangData lang : LangData.values()) {
            pvd.add(lang.key, lang.def);
        }
    }
}
