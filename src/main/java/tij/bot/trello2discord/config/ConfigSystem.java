package tij.bot.trello2discord.config;

import java.io.File;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Optional;
import java.util.function.Consumer;

public final class ConfigSystem {
    private enum ReloadMode {
        APPLY,
        RESTART_REQUIRED
    }

    private static final String CONFIG_FILE_NAME = "config.xml";
    private static final File CONFIG_FILE = new File(CONFIG_FILE_NAME);
    private static volatile Config lastConfig;

    private static Thread configWatcherThread;

    private ConfigSystem() {}

    public static Optional<Config> loadConfig() {
        try {
            if (Files.notExists(CONFIG_FILE.toPath())) {
                copyDefaultConfig();
                System.out.println("No config file found. Copying default file.");
                return Optional.empty();
            }

            return Optional.of(parseConfig());
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static void startWatcher(Consumer<Config> reloadHandler) {
        if (configWatcherThread != null && configWatcherThread.isAlive()) {
            return;
        }

        configWatcherThread = new Thread(() -> {
            try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
                Path dir = CONFIG_FILE.toPath().toAbsolutePath().getParent();

                dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key = watcher.take();

                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path changed = (Path) event.context();

                        if (!changed.getFileName().toString().equals(CONFIG_FILE_NAME)) {
                            continue;
                        }

                        try {
                            Thread.sleep(200);

                            Config cfg = parseConfig();

                            ReloadMode mode = validate(lastConfig, cfg);

                            lastConfig = cfg;

                            switch (mode) {
                                case APPLY -> {
                                    System.out.println("Config reloaded.");
                                    reloadHandler.accept(cfg);
                                }
                                case RESTART_REQUIRED -> {
                                    System.err.println("Critical config change detected (token/port). Restart required.");
                                    reloadHandler.accept(cfg);
                                    System.exit(42);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Config reload failed: ");
                            e.printStackTrace();
                        }
                    }

                    if (!key.reset()) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        configWatcherThread.setName("Config-Watcher");
        configWatcherThread.setDaemon(true);
        configWatcherThread.start();
    }

    public static void stopWatcher() {
        if (configWatcherThread != null) {
            configWatcherThread.interrupt();
        }
    }

    private static Config parseConfig() {
        return ConfigParser.parse(ConfigSystem.CONFIG_FILE);
    }

    private static void copyDefaultConfig() throws Exception {
        try (InputStream is = ConfigSystem.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
            if (is == null) {
                throw new IllegalStateException("Default config not found in resources: " + CONFIG_FILE_NAME);
            }

            Files.copy(
                    is,
                    CONFIG_FILE.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private static ReloadMode validate(Config oldCfg, Config newCfg) {
        if (oldCfg == null)
            return ReloadMode.APPLY;

        if (!oldCfg.discordBotToken().equals(newCfg.discordBotToken()))
            return ReloadMode.RESTART_REQUIRED;

        if (oldCfg.port() != newCfg.port())
            return ReloadMode.RESTART_REQUIRED;

        return ReloadMode.APPLY;
    }
}
