package org.originsd.lexibangla.config;

import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads build metadata populated by Maven resource filtering.
 */
public final class BuildInfo {

    private static final Logger log = LoggerFactory.getLogger(BuildInfo.class);
    private static final Properties properties = new Properties();

    private static final String DEFAULT_VERSION = "1.0.1";

    static {
        try (InputStream in = BuildInfo.class.getResourceAsStream("/build-info.properties")) {
            if (in != null) {
                properties.load(in);
            } else {
                log.warn("build-info.properties not found on classpath, using defaults");
            }
        } catch (Exception e) {
            log.error("Failed to load build-info.properties", e);
        }
    }

    private BuildInfo() {}

    public static String getVersion() {
        String ver = properties.getProperty("application.version");
        if (ver == null || ver.isBlank() || ver.contains("${")) {
            return DEFAULT_VERSION;
        }
        return ver.trim();
    }

    public static String getApplicationName() {
        return properties.getProperty("application.name", AppConfig.APP_NAME);
    }

    public static String getBuildTimestamp() {
        return properties.getProperty("build.timestamp", "Development Build");
    }

    public static String getDescription() {
        return properties.getProperty("application.description", AppConfig.DESCRIPTION);
    }
}
