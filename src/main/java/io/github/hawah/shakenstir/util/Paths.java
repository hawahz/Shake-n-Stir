package io.github.hawah.shakenstir.util;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class Paths {
    public static final Path GAME_DIR = FMLPaths.GAMEDIR.get();
    public static final Path ADDON_DIR = GAME_DIR.resolve("shakenstir");
}
