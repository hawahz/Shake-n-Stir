package io.github.hawah.shakenstir.util;

import com.mojang.logging.LogUtils;
import io.github.hawah.shakenstir.foundation.networking.ServerboundRequestBackgroundPacket;
import io.github.hawah.shakenstir.lib.networking.Networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MenuBackgroundUtils {
    public static final Path BKG_SAVE_PATH = Paths.ADDON_DIR.resolve("menuBackground");
    public static final Path BKG_SAVE_UPLOAD_PATH = Paths.ADDON_DIR.resolve("menuBackground").resolve("upload");
    public static final ConcurrentHashMap<String, DataWarper> requesters = new ConcurrentHashMap<>();
    public static void requestBackground(String name, DataWarper warper, boolean isClientSide, UUID requester) {
        Path path = BKG_SAVE_PATH.resolve(name).toAbsolutePath();
        if (Files.exists(path)) {
            load(path).ifPresent(warper::read);
        } else if (!isClientSide) {
        } else {
            requesters.put(name, warper);
            Networking.sendToServer(new ServerboundRequestBackgroundPacket(name, requester));
        }
    }

    public static void update(String name, int[] data) {
        Optional.ofNullable(requesters.remove(name)).ifPresent(
                warper -> warper.read(data)
        );
    }

    public static void save(String name, int[] data, boolean upload) {
        Path dir = upload ? BKG_SAVE_UPLOAD_PATH : BKG_SAVE_PATH;
        Path path = dir.resolve(name).toAbsolutePath();

        try {
            Files.createDirectories(dir);
            try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(
                    Files.newOutputStream(path, StandardOpenOption.CREATE)))) {
                out.writeInt(data.length);
                for (int value : data) {
                    out.writeInt(value);
                }
            }
        } catch (IOException e) {
            LogUtils.getLogger().error("Occurred Error when saving background.", e);
        }
    }

    public static Optional<int[]> load(Path path) {
        if (Files.exists(path)) {
            try (DataInputStream in = new DataInputStream(new GZIPInputStream(
                    Files.newInputStream(path, StandardOpenOption.READ)))) {
                int[] data = new int[in.readInt()];
                for (int i = 0; i < data.length; i++) {
                    data[i] = in.readInt();
                }
                return Optional.of(data);
            } catch (IOException e) {
                LogUtils.getLogger().error("Occurred Error when loading background.", e);
            }
        }
        return Optional.empty();
    }

    public interface DataWarper {
        void read(int[] data);
    }

}
