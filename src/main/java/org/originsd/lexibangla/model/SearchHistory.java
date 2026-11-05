package org.originsd.lexibangla.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "search_history", indexes = {
    @Index(name = "idx_history_search_time", columnList = "search_time")
})
public class SearchHistory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "query", nullable = false, length = 255)
    private String query;

    @Column(name = "search_time", nullable = false)
    private Instant searchTime;

    public SearchHistory() {
    }

    public SearchHistory(String query) {
        this.query = Objects.requireNonNull(query, "query must not be null").trim();
        this.searchTime = Instant.now();
    }

    public SearchHistory(String query, Instant searchTime) {
        this.query = query;
        this.searchTime = searchTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Instant getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(Instant searchTime) {
        this.searchTime = searchTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchHistory that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SearchHistory{" + "id=" + id + ", query='" + query + '\'' + ", searchTime=" + searchTime + '}';
    }
}
