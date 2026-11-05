package org.originsd.lexibangla.db;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.originsd.lexibangla.model.DictionaryEntry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MasterDictionaryDaoTest {

    private static MasterDictionaryDao dao;

    @BeforeAll
    static void setUp() {
        Path dbPath = Paths.get("src", "main", "resources", "dictionary.sqlite");
        assertTrue(dbPath.toFile().exists(), "dictionary.sqlite should exist in src/main/resources");
        dao = new MasterDictionaryDao(dbPath);
    }

    @AfterAll
    static void tearDown() {
        if (dao != null) {
            dao.close();
        }
    }

    @Test
    @DisplayName("Verify master database record count matches 93,421")
    void testRecordCount() {
        long count = dao.getTotalWordCount();
        assertEquals(93421, count, "Master dictionary should contain exactly 93,421 entries");
    }

    @Test
    @DisplayName("Verify exact search returns correct Bengali meaning")
    void testExactSearch() {
        List<DictionaryEntry> entries = dao.findExact("abandon");
        assertThat(entries).isNotEmpty();
        DictionaryEntry entry = entries.getFirst();
        assertEquals("abandon", entry.word().toLowerCase());
        assertEquals("পরিত্যাগ করা", entry.meaning());
        assertTrue(entry.wordId() > 0);
    }

    @Test
    @DisplayName("Verify search is case-insensitive")
    void testCaseInsensitivity() {
        List<DictionaryEntry> lower = dao.findExact("abandon");
        List<DictionaryEntry> upper = dao.findExact("ABANDON");
        List<DictionaryEntry> mixed = dao.findExact("AbAnDoN");

        assertThat(lower).isNotEmpty();
        assertEquals(lower.getFirst().meaning(), upper.getFirst().meaning());
        assertEquals(lower.getFirst().meaning(), mixed.getFirst().meaning());
    }

    @Test
    @DisplayName("Verify prefix search works and orders by word length and alphabet")
    void testPrefixSearch() {
        List<DictionaryEntry> entries = dao.findPrefix("abandon", 10);
        assertThat(entries).isNotEmpty();
        assertTrue(entries.stream().allMatch(e -> e.word().toLowerCase().startsWith("abandon")));
        assertEquals("abandon", entries.getFirst().word().toLowerCase());
    }

    @Test
    @DisplayName("Verify partial search finds words containing substring")
    void testPartialSearch() {
        List<DictionaryEntry> entries = dao.findPartial("cycl", 10);
        assertThat(entries).isNotEmpty();
        assertTrue(entries.stream().allMatch(e -> e.word().toLowerCase().contains("cycl")));
    }

    @Test
    @DisplayName("Verify Bengali Unicode characters are properly preserved without corruption")
    void testBengaliUnicodeIntegrity() {
        Optional<DictionaryEntry> opt = dao.findByWord("aerodrome");
        assertTrue(opt.isPresent());
        assertEquals("বিমানশালা", opt.get().meaning());
    }

    @Test
    @DisplayName("Verify browsing entries with pagination")
    void testBrowseEntries() {
        List<DictionaryEntry> page1 = dao.browseEntries(5, 0);
        List<DictionaryEntry> page2 = dao.browseEntries(5, 5);

        assertEquals(5, page1.size());
        assertEquals(5, page2.size());
        assertNotEquals(page1.getFirst().wordId(), page2.getFirst().wordId());
    }
}
