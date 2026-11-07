package org.originsd.lexibangla.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.originsd.lexibangla.config.AppConfig;
import org.originsd.lexibangla.config.BuildInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for checking and parsing updates from GitHub Releases API.
 * Compares versions using Semantic Versioning.
 */
public class UpdateService {

    private static final Logger log = LoggerFactory.getLogger(UpdateService.class);

    private final ObjectMapper mapper;
    private final String releasesApiUrl;

    public record UpdateCheckResult(
            boolean updateAvailable,
            String currentVersion,
            String latestVersion,
            String releaseNotes,
            String releaseUrl,
            Optional<ReleaseInfo.ReleaseAsset> matchingAsset
    ) {}

    public UpdateService() {
        this(AppConfig.RELEASES_API_URL);
    }

    public UpdateService(String releasesApiUrl) {
        this.releasesApiUrl = releasesApiUrl;
        this.mapper = new ObjectMapper();
    }

    public CompletableFuture<UpdateCheckResult> checkForUpdatesAsync() {
        return CompletableFuture.supplyAsync(this::checkForUpdates);
    }

    public UpdateCheckResult checkForUpdates() {
        String currentVersion = BuildInfo.getVersion();
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(releasesApiUrl).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "LexiBangla/" + currentVersion);
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);

            int status = connection.getResponseCode();
            if (status == 403 || status == 429) {
                log.warn("GitHub API rate limit reached (HTTP {})", status);
                return new UpdateCheckResult(false, currentVersion, currentVersion, "Rate limited by GitHub API.", "", Optional.empty());
            } else if (status != 200) {
                log.warn("GitHub API returned non-OK status: {}", status);
                return new UpdateCheckResult(false, currentVersion, currentVersion, "Could not check for updates (HTTP " + status + ")", "", Optional.empty());
            }

            try (InputStream in = connection.getInputStream()) {
                ReleaseInfo release = mapper.readValue(in, ReleaseInfo.class);
                if (release.isDraft() || release.isPrerelease()) {
                    return new UpdateCheckResult(false, currentVersion, currentVersion, "Latest release is draft or pre-release.", "", Optional.empty());
                }

                String latestTag = release.getTagName() != null ? release.getTagName().replaceFirst("^v", "") : "";
                boolean newer = isNewerVersion(currentVersion, latestTag);
                Optional<ReleaseInfo.ReleaseAsset> matchedAsset = findPlatformAsset(release);

                return new UpdateCheckResult(
                        newer,
                        currentVersion,
                        latestTag,
                        release.getBody() != null ? release.getBody() : "",
                        release.getHtmlUrl() != null ? release.getHtmlUrl() : "",
                        matchedAsset
                );
            }
        } catch (Exception e) {
            log.info("Update check skipped or failed (offline / unreachable): {}", e.getMessage());
            return new UpdateCheckResult(false, currentVersion, currentVersion, "Offline or server unreachable", "", Optional.empty());
        }
    }

    /**
     * Compares two semantic versions (e.g. "1.0.1" and "1.1.0").
     * Returns true if target is strictly newer than current.
     * If the versions match (including matching release to SNAPSHOT), returns false.
     */
    public static boolean isNewerVersion(String current, String target) {
        if (target == null || target.isBlank()) return false;
        if (current == null || current.isBlank()) return true;

        String cleanCurrent = current.replaceFirst("^v", "").replaceAll("-SNAPSHOT$", "").trim();
        String cleanTarget = target.replaceFirst("^v", "").replaceAll("-SNAPSHOT$", "").trim();

        if (cleanCurrent.equalsIgnoreCase(cleanTarget)) {
            return false;
        }

        String[] curParts = cleanCurrent.split("\\.");
        String[] tgtParts = cleanTarget.split("\\.");

        int length = Math.max(curParts.length, tgtParts.length);
        for (int i = 0; i < length; i++) {
            int curNum = 0;
            int tgtNum = 0;
            if (i < curParts.length) {
                try {
                    curNum = Integer.parseInt(curParts[i].replaceAll("[^0-9]", ""));
                } catch (NumberFormatException ignored) {}
            }
            if (i < tgtParts.length) {
                try {
                    tgtNum = Integer.parseInt(tgtParts[i].replaceAll("[^0-9]", ""));
                } catch (NumberFormatException ignored) {}
            }

            if (tgtNum > curNum) return true;
            if (tgtNum < curNum) return false;
        }

        return false;
    }

    /**
     * Finds the platform-appropriate installer asset from release assets.
     */
    public static Optional<ReleaseInfo.ReleaseAsset> findPlatformAsset(ReleaseInfo release) {
        if (release == null || release.getAssets() == null || release.getAssets().isEmpty()) {
            return Optional.empty();
        }

        String os = System.getProperty("os.name", "").toLowerCase();
        for (ReleaseInfo.ReleaseAsset asset : release.getAssets()) {
            String name = asset.getName().toLowerCase();
            if (os.contains("win")) {
                if (name.endsWith(".msi") || name.endsWith(".exe")) {
                    return Optional.of(asset);
                }
            } else if (os.contains("mac")) {
                if (name.endsWith(".dmg") || name.endsWith(".pkg")) {
                    return Optional.of(asset);
                }
            } else {
                if (name.endsWith(".deb") || name.endsWith(".rpm") || name.endsWith(".tar.gz")) {
                    return Optional.of(asset);
                }
            }
        }
        return Optional.empty();
    }
}
