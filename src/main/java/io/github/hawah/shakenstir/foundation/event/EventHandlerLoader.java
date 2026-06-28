package io.github.hawah.shakenstir.foundation.event;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 运行时 SPI 加载器。
 * <p>
 * 读取构建阶段生成的 SPI 文件
 * ({@code META-INF/services/io.github.hawah.shakenstir.foundation.event.RegisterEvent})，
 * 加载其中列出的所有处理器类，并注册到 {@link SnsEventBus}。
 * <p>
 * 这在编译期注解处理器（或 Gradle 构建任务）的配合下实现了<b>零运行时扫描</b>。
 *
 * @see RegisterEvent
 * @see SnsEventBus#registerHandlerClass(Class)
 */
public final class EventHandlerLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * SPI 文件路径，与 {@link RegisterEvent} 的全限定名对应。
     */
    private static final String SPI_PATH =
            "META-INF/services/" + RegisterEvent.class.getName();

    private EventHandlerLoader() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 从 SPI 文件加载所有处理器类并注册到事件总线。
     *
     * @return 成功注册的处理器类数量
     */
    public static int loadAndRegister() {
        List<String> classNames = readSpiFile();
        if (classNames.isEmpty()) {
            LOGGER.debug("No @RegisterEvent classes found in SPI file: {}", SPI_PATH);
            return 0;
        }

        int count = 0;
        for (String className : classNames) {
            if (className.isBlank() || className.startsWith("#")) {
                continue; // Skip blank lines and comments
            }
            try {
                Class<?> clazz = Class.forName(className.trim(), false,
                        EventHandlerLoader.class.getClassLoader());
                int registered = SnsEventBus.registerHandlerClass(clazz);
                if (registered > 0) {
                    count++;
                    LOGGER.debug("Registered handler class: {} ({} handler(s))",
                            className, registered);
                }
            } catch (ClassNotFoundException e) {
                LOGGER.error("Handler class not found: {}", className, e);
            } catch (NoClassDefFoundError e) {
                LOGGER.debug("Handler class unavailable on this side: {}", className);
            }
        }

        LOGGER.info("EventHandlerLoader loaded {} handler class(es)", count);
        return count;
    }

    /**
     * 读取 SPI 文件中的所有行（类名）。
     */
    private static List<String> readSpiFile() {
        ClassLoader cl = EventHandlerLoader.class.getClassLoader();
        // Try the current classloader first, then context classloader
        InputStream input = cl.getResourceAsStream(SPI_PATH);
        if (input == null) {
            input = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(SPI_PATH);
        }
        if (input == null) {
            LOGGER.debug("SPI file not found (this is normal on first build): {}", SPI_PATH);
            return Collections.emptyList();
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            LOGGER.error("Error reading SPI file: {}", SPI_PATH, e);
        }
        return lines;
    }
}
