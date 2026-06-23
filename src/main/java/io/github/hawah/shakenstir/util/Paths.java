package io.github.hawah.shakenstir.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class Paths {
    public static final Path GAME_DIR = FMLPaths.GAMEDIR.get();
    public static final Path ADDON_DIR = GAME_DIR.resolve("shakenstir");

    // TODO: 人工审查 - 2026-06-22 - 新增对话导出目录常量 CONVERSATION_DIR，指向 ./shakenstir/bartender/conversation/
    /** 对话 JSON 文件导出/加载目录 (Conversation JSON export/load directory) */
    public static final Path CONVERSATION_DIR = GAME_DIR.resolve("shakenstir/bartender/conversation");

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
