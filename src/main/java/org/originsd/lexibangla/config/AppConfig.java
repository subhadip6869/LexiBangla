package org.originsd.lexibangla.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global application configuration and environment detection.
 */
public final class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    public static final String APP_NAME = "LexiBangla Desktop";
    public static final String PUBLISHER = "OriginSD";
    public static final String DEVELOPER = "Subhadip Dutta";
    public static final String WEBSITE_URL = "https://originsd.org";
    public static final String REPO_URL = "https://github.com/subhadip6869/LexiBangla.git";
    public static final String RELEASES_API_URL = "https://api.github.com/repos/subhadip6869/LexiBangla/releases/latest";
    public static final String LOCATION = "Bankura, West Bengal, India";
    public static final String TAGLINE = "Words Connect Worlds.";
    public static final String DESCRIPTION = "English-to-Bengali Offline Dictionary";
    public static final String COPYRIGHT = "© 2026 OriginSD";

    public static final String MASTER_DB_FILENAME = "dictionary.sqlite";
    public static final String USER_DB_FILENAME = "lexibangla-user.sqlite";
    public static final String SETTINGS_FILENAME = "settings.json";

    private AppConfig() {}

    /**
     * Resolves the OS-appropriate application data directory:
     * - Windows: %LOCALAPPDATA%/OriginSD/LexiBangla/
     * - Linux: ~/.local/share/lexibangla/
     * - macOS: ~/Library/Application Support/LexiBangla/
     */
    public static Path getUserDataDirectory() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        Path dir;

        if (osName.contains("win")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData == null || localAppData.isBlank()) {
                localAppData = System.getProperty("user.home") + "\\AppData\\Local";
            }
            dir = Paths.get(localAppData, "OriginSD", "LexiBangla");
        } else if (osName.contains("mac")) {
            dir = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "LexiBangla");
        } else {
            // Linux and other UNIX systems (XDG Base Directory standard)
            String xdgDataHome = System.getenv("XDG_DATA_HOME");
            if (xdgDataHome != null && !xdgDataHome.isBlank()) {
                dir = Paths.get(xdgDataHome, "lexibangla");
            } else {
                dir = Paths.get(System.getProperty("user.home"), ".local", "share", "lexibangla");
            }
        }

        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                log.info("Created user data directory: {}", dir);
            }
        } catch (Exception e) {
            log.error("Failed to create user data directory: {}", dir, e);
        }

        return dir;
    }

    public static Path getUserDatabasePath() {
        return getUserDataDirectory().resolve(USER_DB_FILENAME);
    }

    public static Path getSettingsPath() {
        return getUserDataDirectory().resolve(SETTINGS_FILENAME);
    }
}
