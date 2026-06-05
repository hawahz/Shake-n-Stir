package io.github.hawah.shakenstir.util;

import com.mojang.logging.LogUtils;
import io.github.hawah.shakenstir.foundation.networking.ServerboundRequestBackgroundPacket;
import io.github.hawah.shakenstir.lib.networking.Networking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

public class MenuBackgroundUtils {
    public static final Path BKG_SAVE_PATH = Paths.ADDON_DIR.resolve("menuBackground");
    public static final Path BKG_SAVE_UPLOAD_PATH = Paths.ADDON_DIR.resolve("menuBackground").resolve("upload");
    public static final ConcurrentHashMap<String, DataWarper> requesters = new ConcurrentHashMap<>();
    public static void requestBackground(String name, DataWarper warper, boolean isClientSide, UUID requester) {
        Path path = BKG_SAVE_PATH.resolve(name).toAbsolutePath();
        if (Files.exists(path)) {
            try (DataInputStream stream = new DataInputStream(new BufferedInputStream(
                    new GZIPInputStream(Files.newInputStream(path, StandardOpenOption.READ))))) {
                CompoundTag nbt = NbtIo.read(stream, NbtAccounter.create(0x20000000L));
                nbt.getIntArray("data").ifPresent(
                        warper::read
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (!isClientSide) {
            return;
        } else {
            requesters.put(name, warper);
            Networking.sendToServer(new ServerboundRequestBackgroundPacket(name, requester));
        }
    }

    public static void save(String name, int[] data, boolean upload) {
        Path dir = upload ? BKG_SAVE_UPLOAD_PATH : BKG_SAVE_PATH;
        Path path = dir.resolve(name).toAbsolutePath();
        CompoundTag tag = new CompoundTag();
        tag.putIntArray("data", data);

        try {
            Files.createDirectories(dir);
            try (OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE)) {
                NbtIo.writeCompressed(tag, out);
            }
        } catch (IOException e) {
            LogUtils.getLogger().error("Occurred Error when saving background.", e);
        }
    }

    public static void update(String name, int[] data) {
        Optional.ofNullable(requesters.remove(name)).ifPresent(
                warper -> warper.read(data)
        );
    }

    public interface DataWarper {
        void read(int[] data);
    }

}
