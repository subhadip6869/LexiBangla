package org.originsd.lexibangla.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable representation of an English-to-Bengali master dictionary entry.
 * Corresponds exactly to the schema: bangla_dictionary(word_id, word, meaning).
 */
public record DictionaryEntry(long wordId, String word, String meaning) implements Serializable {

    public DictionaryEntry {
        Objects.requireNonNull(word, "word must not be null");
        Objects.requireNonNull(meaning, "meaning must not be null");
    }

    /**
     * Formatted string for clipboard and display export.
     */
    public String toFullEntryString() {
        return word + " : " + meaning;
    }
}
