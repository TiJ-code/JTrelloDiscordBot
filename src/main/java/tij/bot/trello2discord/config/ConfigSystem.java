package tij.bot.trello2discord.config;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public final class ConfigSystem {
    private static final String CONFIG_FILE_NAME = "config.xml";
    private static final File CONFIG_FILE = new File(CONFIG_FILE_NAME);

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
}
