package io.github.hawah.shakenstir.content.recipe;

import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import io.github.hawah.shakenstir.content.fluid.FluidTypeRegistries;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Locale;

public enum Spirits implements StringRepresentable {
    GIN(FluidRegistries.GIN_SOURCE_FLUID_BLOCK, LangData.GIN), // 5, 0 ,5; 13, 10, 13
    WHISKY(FluidRegistries.WHISKY_SOURCE_FLUID_BLOCK, LangData.WHISKEY),
    VODKA(FluidRegistries.VODKA_SOURCE_FLUID_BLOCK, LangData.VODKA),
    RUM(FluidRegistries.RUM_SOURCE_FLUID_BLOCK, LangData.RUM),
    TEQUILA(FluidRegistries.TEQUILA_SOURCE_FLUID_BLOCK, LangData.TEQUILA),
    BRANDY(FluidRegistries.BRANDY_SOURCE_FLUID_BLOCK, LangData.BRANDY),
    ;

    private final DeferredHolder<Fluid, FlowingFluid> fluid;
    private final LangData langData;

    Spirits(DeferredHolder<Fluid, FlowingFluid> fluid, LangData langData) {
        this.fluid = fluid;
        this.langData = langData;
    }

    public Fluid getFluid() {
        return fluid.get();
    }
    public MutableComponent getTranslatable() {
        return langData.get();
    }

    public static Spirits fromFluid(FluidType fluidType) {
        if (FluidTypeRegistries.GIN_FLUID_TYPE.get().equals(fluidType)) {
            return GIN;
        }
        if (FluidTypeRegistries.VODKA_FLUID_TYPE.get().equals(fluidType)) {
            return              VODKA;
        }
        if (FluidTypeRegistries.BRANDY_FLUID_TYPE.get().equals(fluidType)) {
            return              BRANDY;
        }
        if (FluidTypeRegistries.RUM_FLUID_TYPE.get().equals(fluidType)) {
            return              RUM;
        }
        if (FluidTypeRegistries.TEQUILA_FLUID_TYPE.get().equals(fluidType)) {
            return              TEQUILA;
        }
        if (FluidTypeRegistries.WHISKY_FLUID_TYPE.get().equals(fluidType)) {
            return              WHISKY;
        }
        return GIN;
    }

    public static MutableComponent from(FluidType fluidType) {
        if (FluidTypeRegistries.GIN_FLUID_TYPE.get().equals(fluidType)) {
            return GIN.getTranslatable();
        }
        if (FluidTypeRegistries.VODKA_FLUID_TYPE.get().equals(fluidType)) {
            return              VODKA.getTranslatable();
        }
        if (FluidTypeRegistries.BRANDY_FLUID_TYPE.get().equals(fluidType)) {
            return              BRANDY.getTranslatable();
        }
        if (FluidTypeRegistries.RUM_FLUID_TYPE.get().equals(fluidType)) {
            return              RUM.getTranslatable();
        }
        if (FluidTypeRegistries.TEQUILA_FLUID_TYPE.get().equals(fluidType)) {
            return              TEQUILA.getTranslatable();
        }
        if (FluidTypeRegistries.WHISKY_FLUID_TYPE.get().equals(fluidType)) {
            return              WHISKY.getTranslatable();
        }
        return Component.empty();
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
