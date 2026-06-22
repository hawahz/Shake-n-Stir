package io.github.hawah.shakenstir.content.dialogue;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 对话数据 (Dialogue Data)，包含酒保实体的全部对话条目配置。
 * 作为实体持久化数据存储，同时也可通过网络同步到客户端用于编辑。
 */
public record DialogueData(List<DialogueEntry> entries) {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DialogueData EMPTY = new DialogueData(List.of());

    /**
     * 创建一个可变副本，允许编辑后重新构建。
     */
    public DialogueData copyMutable() {
        return new DialogueData(new ArrayList<>(entries));
    }

    /**
     * 获取不可变视图。
     */
    public List<DialogueEntry> getEntries() {
        return List.copyOf(entries);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    // TODO: 人工审查 - 2026-06-22 - 新增静态工厂方法 loadFromFile / loadFromResource，用于从文件系统和游戏资源加载 DialogueData

    /**
     * 从文件系统路径加载对话数据 (Load DialogueData from filesystem path)。
     * 使用 {@link #CODEC} + {@link JsonOps#INSTANCE} 解析 JSON。
     *
     * @param filePath JSON 文件路径
     * @return 解析成功返回 DialogueData，失败返回 EMPTY
     */
    public static DialogueData loadFromFile(Path filePath) {
        if (!Files.exists(filePath)) {
            LOGGER.trace("DialogueData file not found: {}", filePath);
            return EMPTY;
        }
        try (Reader reader = Files.newBufferedReader(filePath)) {
            JsonElement json = JsonParser.parseReader(reader);
            var result = CODEC.parse(JsonOps.INSTANCE, json);
            return result.resultOrPartial(err ->
                    LOGGER.error("Failed to parse DialogueData from file '{}': {}", filePath, err)
            ).orElse(EMPTY);
        } catch (IOException e) {
            LOGGER.error("Failed to read DialogueData file '{}'", filePath, e);
            return EMPTY;
        } catch (Exception e) {
            LOGGER.error("Unexpected error loading DialogueData from file '{}'", filePath, e);
            return EMPTY;
        }
    }

    /**
     * 从游戏资源（数据包/模组内置资源）加载对话数据 (Load DialogueData from game resource)。
     * 通过 {@link ResourceManager} 读取指定 {@link Identifier} 的 JSON 资源。
     *
     * @param resourceLocation 资源位置，如 {@code shakenstir:dialogue/default.json}
     * @param resourceManager  资源管理器，通过 {@code ServerLevel.getServer().getResourceManager()} 获取
     * @return 解析成功返回 DialogueData，失败返回 EMPTY
     */
    public static DialogueData loadFromResource(Identifier resourceLocation, ResourceManager resourceManager) {
        Optional<Resource> resourceOpt = resourceManager.getResource(resourceLocation);
        if (resourceOpt.isEmpty()) {
            LOGGER.warn("DialogueData resource not found: {}", resourceLocation);
            return EMPTY;
        }
        try (Reader reader = resourceOpt.get().openAsReader()) {
            JsonElement json = JsonParser.parseReader(reader);
            var result = CODEC.parse(JsonOps.INSTANCE, json);
            return result.resultOrPartial(err ->
                    LOGGER.error("Failed to parse DialogueData from resource '{}': {}", resourceLocation, err)
            ).orElse(EMPTY);
        } catch (IOException e) {
            LOGGER.error("Failed to read DialogueData resource '{}'", resourceLocation, e);
            return EMPTY;
        } catch (Exception e) {
            LOGGER.error("Unexpected error loading DialogueData from resource '{}'", resourceLocation, e);
            return EMPTY;
        }
    }

    public static final Codec<DialogueData> CODEC = DialogueEntry.CODEC.listOf()
            .xmap(DialogueData::new, DialogueData::entries);

    public static final StreamCodec<RegistryFriendlyByteBuf, DialogueData> STREAM_CODEC =
            DialogueEntry.STREAM_CODEC.apply(ByteBufCodecs.list())
                    .map(DialogueData::new, DialogueData::entries);
}
