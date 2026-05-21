package io.github.hawah.shakenstir.foundation.block;

import net.minecraft.util.StringRepresentable;

public enum DistillerPart implements StringRepresentable {
    LOWER("lower"),
    UPPER("upper"),
    PIPE("pipe");

    private final String name;

    DistillerPart(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}