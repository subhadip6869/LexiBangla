package org.originsd.lexibangla.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for loading and persisting user preferences to settings.json in the user data directory.
 */
public class UserSettingsService {

    private static final Logger log = LoggerFactory.getLogger(UserSettingsService.class);

    private final ObjectMapper mapper;
    private final Path settingsPath;
    private UserSettings currentSettings;

    public UserSettingsService() {
        this(AppConfig.getSettingsPath());
    }

    public UserSettingsService(Path settingsPath) {
        this.settingsPath = settingsPath;
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.currentSettings = load();
    }

    public synchronized UserSettings getSettings() {
        if (currentSettings == null) {
            currentSettings = load();
        }
        return currentSettings;
    }

    public synchronized void save(UserSettings settings) {
        this.currentSettings = settings;
        try {
            Files.createDirectories(settingsPath.getParent());
            mapper.writeValue(settingsPath.toFile(), settings);
            log.info("Persisted user settings to {}", settingsPath);
        } catch (Exception e) {
            log.error("Failed to save user settings to {}", settingsPath, e);
        }
    }

    private UserSettings load() {
        File file = settingsPath.toFile();
        if (file.exists() && file.length() > 0) {
            try {
                UserSettings s = mapper.readValue(file, UserSettings.class);
                log.info("Loaded user settings from {}", settingsPath);
                return s;
            } catch (Exception e) {
                log.warn("Failed to parse settings file {}, resetting to default", settingsPath, e);
            }
        }
        return new UserSettings();
    }
}
