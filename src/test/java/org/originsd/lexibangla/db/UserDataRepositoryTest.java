package org.originsd.lexibangla.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.originsd.lexibangla.model.Favorite;
import org.originsd.lexibangla.model.PersonalNote;
import org.originsd.lexibangla.model.SearchHistory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UserDataRepositoryTest {

    private static Path tempDbFile;
    private static SessionFactory sessionFactory;
    private static UserDataRepository repo;

    @BeforeAll
    static void setUp() throws IOException {
        tempDbFile = Files.createTempFile("lexibangla-user-test-", ".sqlite");
        sessionFactory = HibernateUtil.buildSessionFactory(tempDbFile);
        repo = new UserDataRepository(sessionFactory);
    }

    @AfterAll
    static void tearDown() throws IOException {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
        if (tempDbFile != null && Files.exists(tempDbFile)) {
            Files.deleteIfExists(tempDbFile);
        }
    }

    @Test
    @DisplayName("Verify adding, querying and removing favorites")
    void testFavoritesCrud() {
        assertFalse(repo.isFavorite("peace"));

        boolean added = repo.addFavorite(100L, "peace");
        assertTrue(added);
        assertTrue(repo.isFavorite("peace"));
        assertTrue(repo.isFavorite("PEACE"), "Favorite check should be case-insensitive");

        // Verify duplicate prevention
        boolean addedAgain = repo.addFavorite(100L, "peace");
        assertFalse(addedAgain, "Duplicate favorite should not be added");

        List<Favorite> favorites = repo.getAllFavorites();
        assertThat(favorites).extracting(Favorite::getWord).contains("peace");
        assertTrue(repo.getFavoriteCount() >= 1);

        boolean removed = repo.removeFavorite("peace");
        assertTrue(removed);
        assertFalse(repo.isFavorite("peace"));
    }

    @Test
    @DisplayName("Verify recording and clearing search history")
    void testSearchHistory() {
        repo.recordSearch("dictionary");
        repo.recordSearch("bengali");

        List<SearchHistory> history = repo.getRecentSearches(10);
        assertThat(history).extracting(SearchHistory::getQuery).contains("bengali", "dictionary");

        boolean cleared = repo.clearSearchHistory();
        assertTrue(cleared);
        assertEquals(0, repo.getSearchHistoryCount());
    }

    @Test
    @DisplayName("Verify personal notes lifecycle (create, update, read, delete)")
    void testPersonalNotes() {
        PersonalNote note1 = repo.saveOrUpdateNote(1013L, "abandon", "Important vocabulary word for essay");
        assertNotNull(note1);
        assertEquals("abandon", note1.getWord());
        assertEquals("Important vocabulary word for essay", note1.getContent());

        Optional<PersonalNote> fetched = repo.getNoteForWord("abandon");
        assertTrue(fetched.isPresent());
        assertEquals("Important vocabulary word for essay", fetched.get().getContent());

        // Update note
        PersonalNote updated = repo.saveOrUpdateNote(1013L, "abandon", "Updated: to leave completely");
        assertNotNull(updated);
        assertEquals("Updated: to leave completely", updated.getContent());

        Optional<PersonalNote> fetchedUpdated = repo.getNoteForWord("abandon");
        assertTrue(fetchedUpdated.isPresent());
        assertEquals("Updated: to leave completely", fetchedUpdated.get().getContent());

        // Delete note
        boolean deleted = repo.deleteNoteForWord("abandon");
        assertTrue(deleted);
        assertFalse(repo.getNoteForWord("abandon").isPresent());
    }
}
