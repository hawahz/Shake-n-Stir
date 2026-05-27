package io.github.hawah.shakenstir.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class Paths {
    public static final Path GAME_DIR = FMLPaths.GAMEDIR.get();
    public static final Path ADDON_DIR = GAME_DIR.resolve("shakenstir");

    public static Path getServerRoot(ServerLevel serverLevel) {
        return serverLevel.getServer().getWorldPath(LevelResource.ROOT).resolve("shakenstir");
    }

    public static Path getPlayerDataPath(ServerLevel serverLevel, ServerPlayer player) {
        return getPlayerDataPath(serverLevel, player.getName().getString());
    }

    public static Path getPlayerDataPath(ServerLevel serverLevel, String player) {
        return getServerRoot(serverLevel).resolve(player);
    }
}
