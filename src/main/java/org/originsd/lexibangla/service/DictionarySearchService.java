package org.originsd.lexibangla.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import javafx.application.Platform;
import org.originsd.lexibangla.db.MasterDictionaryDao;
import org.originsd.lexibangla.model.DictionaryEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * High-performance, asynchronous search service with task cancellation
 * and debouncing. Ensures the JavaFX Application Thread remains fluid.
 */
public class DictionarySearchService {

    private static final Logger log = LoggerFactory.getLogger(DictionarySearchService.class);

    public enum SearchMode {
        SMART,  // Combines exact, prefix, and partial in priority order
        EXACT,
        PREFIX,
        PARTIAL
    }

    private final MasterDictionaryDao masterDao;
    private final ExecutorService executor;
    private Future<?> activeSearchTask;

    public DictionarySearchService(MasterDictionaryDao masterDao) {
        this.masterDao = masterDao;
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Dictionary-Search-Thread");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Executes an asynchronous search query. Outdated tasks are automatically cancelled.
     */
    public synchronized void search(String query, SearchMode mode, int maxResults,
                                   Consumer<List<DictionaryEntry>> onResults,
                                   Consumer<Throwable> onError) {
        if (activeSearchTask != null && !activeSearchTask.isDone()) {
            activeSearchTask.cancel(true);
        }

        if (query == null || query.isBlank()) {
            if (onResults != null) {
                Platform.runLater(() -> onResults.accept(Collections.emptyList()));
            }
            return;
        }

        final String cleanQuery = query.trim();

        activeSearchTask = executor.submit(() -> {
            try {
                if (Thread.currentThread().isInterrupted()) return;

                List<DictionaryEntry> results = switch (mode) {
                    case EXACT -> masterDao.findExact(cleanQuery);
                    case PREFIX -> masterDao.findPrefix(cleanQuery, maxResults);
                    case PARTIAL -> masterDao.findPartial(cleanQuery, maxResults);
                    case SMART -> executeSmartSearch(cleanQuery, maxResults);
                };

                if (!Thread.currentThread().isInterrupted() && onResults != null) {
                    Platform.runLater(() -> onResults.accept(results));
                }
            } catch (Throwable t) {
                if (!Thread.currentThread().isInterrupted()) {
                    log.error("Search error for query '{}'", cleanQuery, t);
                    if (onError != null) {
                        Platform.runLater(() -> onError.accept(t));
                    }
                }
            }
        });
    }

    private List<DictionaryEntry> executeSmartSearch(String query, int maxResults) {
        // MasterDictionaryDao's findPartial already ranks exact = 1, prefix = 2, partial = 3,
        // ordered by length(word) ASC and word ASC!
        return masterDao.findPartial(query, maxResults);
    }

    public void shutdown() {
        if (activeSearchTask != null) {
            activeSearchTask.cancel(true);
        }
        executor.shutdownNow();
    }
}
