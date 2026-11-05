package org.originsd.lexibangla.db;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.originsd.lexibangla.model.Favorite;
import org.originsd.lexibangla.model.PersonalNote;
import org.originsd.lexibangla.model.SearchHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repository for managing user data (Favorites, Search History, Notes)
 * with robust transaction handling and error resilience.
 */
public class UserDataRepository {

    private static final Logger log = LoggerFactory.getLogger(UserDataRepository.class);

    private final SessionFactory sessionFactory;

    public UserDataRepository() {
        this(HibernateUtil.getSessionFactory());
    }

    public UserDataRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    // ==========================================
    // FAVORITES
    // ==========================================

    public boolean addFavorite(long wordId, String word) {
        if (word == null || word.isBlank()) return false;
        String cleanWord = word.trim();

        if (isFavorite(cleanWord)) {
            log.debug("Word '{}' is already in favorites", cleanWord);
            return false;
        }

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            Favorite fav = new Favorite(wordId, cleanWord);
            session.persist(fav);
            tx.commit();
            log.info("Added favorite: '{}'", cleanWord);
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            log.error("Failed to add favorite '{}'", cleanWord, e);
            return false;
        }
    }

    public boolean removeFavorite(String word) {
        if (word == null || word.isBlank()) return false;
        String cleanWord = word.trim();

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            int deleted = session.createMutationQuery("DELETE FROM Favorite f WHERE LOWER(f.word) = LOWER(:word)")
                    .setParameter("word", cleanWord)
                    .executeUpdate();
            tx.commit();
            log.info("Removed favorite: '{}', count: {}", cleanWord, deleted);
            return deleted > 0;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            log.error("Failed to remove favorite '{}'", cleanWord, e);
            return false;
        }
    }

    public boolean isFavorite(String word) {
        if (word == null || word.isBlank()) return false;
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery(
                    "SELECT COUNT(f.id) FROM Favorite f WHERE LOWER(f.word) = LOWER(:word)", Long.class)
                    .setParameter("word", word.trim())
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Failed to check if '{}' is favorite", word, e);
            return false;
        }
    }

    public List<Favorite> getAllFavorites() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Favorite f ORDER BY f.createdAt DESC", Favorite.class).list();
        } catch (Exception e) {
            log.error("Failed to fetch all favorites", e);
            return Collections.emptyList();
        }
    }

    public long getFavoriteCount() {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("SELECT COUNT(f.id) FROM Favorite f", Long.class).uniqueResult();
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("Failed to fetch favorite count", e);
            return 0L;
        }
    }

    // ==========================================
    // SEARCH HISTORY
    // ==========================================

    public void recordSearch(String query) {
        if (query == null || query.isBlank()) return;
        String clean = query.trim();

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            SearchHistory history = new SearchHistory(clean, Instant.now());
            session.persist(history);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            log.error("Failed to record search history for '{}'", clean, e);
        }
    }

    public List<SearchHistory> getRecentSearches(int limit) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM SearchHistory h ORDER BY h.searchTime DESC", SearchHistory.class)
                    .setMaxResults(Math.max(1, limit))
                    .list();
        } catch (Exception e) {
            log.error("Failed to fetch search history", e);
            return Collections.emptyList();
        }
    }

    public boolean clearSearchHistory() {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.createMutationQuery("DELETE FROM SearchHistory").executeUpdate();
            tx.commit();
            log.info("Cleared search history");
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            log.error("Failed to clear search history", e);
            return false;
        }
    }

    public boolean deleteSearchHistoryItem(Long id) {
        if (id == null) return false;
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            SearchHistory item = session.get(SearchHistory.class, id);
            if (item != null) {
                session.remove(item);
                tx.commit();
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            log.error("Failed to delete search history item ID {}", id, e);
            return false;
        }
    }

    public long getSearchHistoryCount() {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("SELECT COUNT(h.id) FROM SearchHistory h", Long.class).uniqueResult();
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("Failed to fetch search history count", e);
            return 0L;
        }
    }

    // ==========================================
    // PERSONAL NOTES
    // ==========================================

    public PersonalNote saveOrUpdateNote(long wordId, String word, String content) {
        if (word == null || word.isBlank()) return null;
        String cleanWord = word.trim();
        String cleanContent = content != null ? content.trim() : "";

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            PersonalNote existing = session.createQuery(
                    "FROM PersonalNote n WHERE LOWER(n.word) = LOWER(:word)", PersonalNote.class)
                    .setParameter("word", cleanWord)
                    .uniqueResult();

            if (existing != null) {
                existing.setContent(cleanContent);
                existing.setWordId(wordId);
                session.merge(existing);
                tx.commit();
                log.info("Updated note for '{}'", cleanWord);
                return existing;
            } else {
                PersonalNote newNote = new PersonalNote(wordId, cleanWord, cleanContent);
                session.persist(newNote);
                tx.commit();
                log.info("Created note for '{}'", cleanWord);
                return newNote;
            }
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            log.error("Failed to save or update note for '{}'", cleanWord, e);
            return null;
        }
    }

    public Optional<PersonalNote> getNoteForWord(String word) {
        if (word == null || word.isBlank()) return Optional.empty();
        try (Session session = sessionFactory.openSession()) {
            PersonalNote note = session.createQuery(
                    "FROM PersonalNote n WHERE LOWER(n.word) = LOWER(:word)", PersonalNote.class)
                    .setParameter("word", word.trim())
                    .uniqueResult();
            return Optional.ofNullable(note);
        } catch (Exception e) {
            log.error("Failed to get note for '{}'", word, e);
            return Optional.empty();
        }
    }

    public List<PersonalNote> getAllNotes() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM PersonalNote n ORDER BY n.updatedAt DESC", PersonalNote.class).list();
        } catch (Exception e) {
            log.error("Failed to fetch all notes", e);
            return Collections.emptyList();
        }
    }

    public boolean deleteNote(Long id) {
        if (id == null) return false;
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            PersonalNote note = session.get(PersonalNote.class, id);
            if (note != null) {
                session.remove(note);
                tx.commit();
                log.info("Deleted note with ID {}", id);
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            log.error("Failed to delete note with ID {}", id, e);
            return false;
        }
    }

    public boolean deleteNoteForWord(String word) {
        if (word == null || word.isBlank()) return false;
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            int deleted = session.createMutationQuery("DELETE FROM PersonalNote n WHERE LOWER(n.word) = LOWER(:word)")
                    .setParameter("word", word.trim())
                    .executeUpdate();
            tx.commit();
            log.info("Deleted note for word '{}', count: {}", word, deleted);
            return deleted > 0;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            log.error("Failed to delete note for word '{}'", word, e);
            return false;
        }
    }

    public long getNoteCount() {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery("SELECT COUNT(n.id) FROM PersonalNote n", Long.class).uniqueResult();
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("Failed to fetch note count", e);
            return 0L;
        }
    }
}
