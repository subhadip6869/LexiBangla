package org.originsd.lexibangla.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "favorites", uniqueConstraints = {
    @UniqueConstraint(name = "uk_favorite_word", columnNames = {"word"})
})
public class Favorite implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "word_id", nullable = false)
    private Long wordId;

    @Column(name = "word", nullable = false, length = 255)
    private String word;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Favorite() {
    }

    public Favorite(Long wordId, String word) {
        this.wordId = Objects.requireNonNull(wordId, "wordId must not be null");
        this.word = Objects.requireNonNull(word, "word must not be null").trim();
        this.createdAt = Instant.now();
    }

    public Favorite(Long wordId, String word, Instant createdAt) {
        this.wordId = wordId;
        this.word = word;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Favorite favorite)) return false;
        return Objects.equals(word, favorite.word);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word);
    }

    @Override
    public String toString() {
        return "Favorite{" + "id=" + id + ", wordId=" + wordId + ", word='" + word + '\'' + ", createdAt=" + createdAt + '}';
    }
}
