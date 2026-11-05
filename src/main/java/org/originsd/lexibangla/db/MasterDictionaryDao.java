package org.originsd.lexibangla.db;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.originsd.lexibangla.config.AppConfig;
import org.originsd.lexibangla.model.DictionaryEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

/**
 * High-performance, read-only SQLite JDBC DAO for the master dictionary.
 * Does not use ORM/Hibernate entity hydration to ensure maximum search speed and low memory footprint.
 */
public class MasterDictionaryDao implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(MasterDictionaryDao.class);

    private static final String EXACT_QUERY =
            "SELECT word_id, word, meaning FROM bangla_dictionary WHERE LOWER(word) = LOWER(?) LIMIT 100";

    private static final String PREFIX_QUERY =
            "SELECT word_id, word, meaning FROM bangla_dictionary WHERE LOWER(word) LIKE LOWER(?) || '%' " +
            "ORDER BY length(word) ASC, word ASC LIMIT ?";

    private static final String PARTIAL_QUERY =
            "SELECT word_id, word, meaning FROM bangla_dictionary WHERE LOWER(word) LIKE '%' || LOWER(?) || '%' " +
            "ORDER BY CASE WHEN LOWER(word) = LOWER(?) THEN 1 WHEN LOWER(word) LIKE LOWER(?) || '%' THEN 2 ELSE 3 END, " +
            "length(word) ASC, word ASC LIMIT ?";

    private static final String FIND_BY_ID_QUERY =
            "SELECT word_id, word, meaning FROM bangla_dictionary WHERE word_id = ?";

    private static final String FIND_BY_WORD_QUERY =
            "SELECT word_id, word, meaning FROM bangla_dictionary WHERE LOWER(word) = LOWER(?) LIMIT 1";

    private static final String COUNT_QUERY =
            "SELECT COUNT(*) FROM bangla_dictionary";

    private static final String BROWSE_QUERY =
            "SELECT word_id, word, meaning FROM bangla_dictionary ORDER BY word ASC LIMIT ? OFFSET ?";

    private final SQLiteDataSource dataSource;
    private final Path databasePath;

    public MasterDictionaryDao() {
        this(resolveDefaultDatabasePath());
    }

    public MasterDictionaryDao(Path databasePath) {
        this.databasePath = databasePath;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("SQLite JDBC Driver not found on classpath", e);
        }

        SQLiteConfig config = new SQLiteConfig();
        config.setReadOnly(true);
        config.setJournalMode(SQLiteConfig.JournalMode.OFF);
        config.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
        config.setCacheSize(10000);
        config.setTempStore(SQLiteConfig.TempStore.MEMORY);

        this.dataSource = new SQLiteDataSource(config);
        this.dataSource.setUrl("jdbc:sqlite:" + databasePath.toAbsolutePath().toString().replace('\\', '/'));
        log.info("Initialized MasterDictionaryDao with database: {}", databasePath);
    }

    /**
     * Resolves the master dictionary database path.
     * In development: directly uses src/main/resources/dictionary.sqlite if present.
     * In packaging/production: extracts from classpath to user data directory if needed.
     */
    public static Path resolveDefaultDatabasePath() {
        // 1. Check local source tree (development mode)
        Path devPath = Paths.get("src", "main", "resources", AppConfig.MASTER_DB_FILENAME);
        if (Files.exists(devPath)) {
            return devPath.toAbsolutePath();
        }

        // 2. Check current working directory
        Path localPath = Paths.get(AppConfig.MASTER_DB_FILENAME);
        if (Files.exists(localPath)) {
            return localPath.toAbsolutePath();
        }

        // 3. User data directory (production/extracted mode)
        Path targetPath = AppConfig.getUserDataDirectory().resolve(AppConfig.MASTER_DB_FILENAME);
        if (Files.exists(targetPath)) {
            return targetPath;
        }

        // Extract from classpath
        try (InputStream in = MasterDictionaryDao.class.getResourceAsStream("/" + AppConfig.MASTER_DB_FILENAME)) {
            if (in == null) {
                throw new IllegalStateException("Dictionary database " + AppConfig.MASTER_DB_FILENAME + " not found on classpath!");
            }
            Files.createDirectories(targetPath.getParent());
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Extracted master dictionary to {}", targetPath);
            return targetPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to extract master dictionary database", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public List<DictionaryEntry> findExact(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        String cleanQuery = query.trim();
        List<DictionaryEntry> results = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(EXACT_QUERY)) {
            stmt.setString(1, cleanQuery);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to execute findExact for '{}'", cleanQuery, e);
        }
        return results;
    }

    public List<DictionaryEntry> findPrefix(String prefix, int limit) {
        if (prefix == null || prefix.isBlank()) {
            return Collections.emptyList();
        }
        String cleanPrefix = prefix.trim();
        List<DictionaryEntry> results = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(PREFIX_QUERY)) {
            stmt.setString(1, cleanPrefix);
            stmt.setInt(2, Math.max(1, limit));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to execute findPrefix for '{}'", cleanPrefix, e);
        }
        return results;
    }

    public List<DictionaryEntry> findPartial(String partial, int limit) {
        if (partial == null || partial.isBlank()) {
            return Collections.emptyList();
        }
        String clean = partial.trim();
        List<DictionaryEntry> results = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(PARTIAL_QUERY)) {
            stmt.setString(1, clean);
            stmt.setString(2, clean);
            stmt.setString(3, clean);
            stmt.setInt(4, Math.max(1, limit));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to execute findPartial for '{}'", clean, e);
        }
        return results;
    }

    public Optional<DictionaryEntry> findById(long wordId) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_QUERY)) {
            stmt.setLong(1, wordId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find word by ID {}", wordId, e);
        }
        return Optional.empty();
    }

    public Optional<DictionaryEntry> findByWord(String word) {
        if (word == null || word.isBlank()) {
            return Optional.empty();
        }
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_WORD_QUERY)) {
            stmt.setString(1, word.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find word by text '{}'", word, e);
        }
        return Optional.empty();
    }

    public List<DictionaryEntry> browseEntries(int limit, int offset) {
        List<DictionaryEntry> results = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(BROWSE_QUERY)) {
            stmt.setInt(1, Math.max(1, limit));
            stmt.setInt(2, Math.max(0, offset));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to browse dictionary entries", e);
        }
        return results;
    }

    public long getTotalWordCount() {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_QUERY);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            log.error("Failed to query total word count", e);
        }
        return 0L;
    }

    public Path getDatabasePath() {
        return databasePath;
    }

    private DictionaryEntry mapRow(ResultSet rs) throws SQLException {
        return new DictionaryEntry(
                rs.getLong("word_id"),
                rs.getString("word"),
                rs.getString("meaning")
        );
    }

    @Override
    public void close() {
        // No persistent connection pool to clean up; individual connections are closed after query
    }
}
