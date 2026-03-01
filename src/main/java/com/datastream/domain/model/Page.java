package com.datastream.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Framework-agnostic pagination container used by repository query results.
 *
 * @param <T> the type of domain object contained in this page
 */
public record Page<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements) {

    /**
     * Compact constructor — validates inputs and wraps the content list as an unmodifiable view.
     *
     * @param content       the items on this page; must not be null
     * @param pageNumber    zero-based page index; must be >= 0
     * @param pageSize      maximum items per page; must be > 0
     * @param totalElements total number of items across all pages; must be >= 0
     */
    public Page {
        Objects.requireNonNull(content, "Page content must not be null");
        if (pageNumber < 0) throw new IllegalArgumentException("pageNumber must be >= 0");
        if (pageSize <= 0)  throw new IllegalArgumentException("pageSize must be > 0");
        if (totalElements < 0) throw new IllegalArgumentException("totalElements must be >= 0");
        content = List.copyOf(content);
    }

    /**
     * Returns the total number of pages.
     *
     * @return number of pages, always at least 1 when there is content
     */
    public int totalPages() {
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    /**
     * Returns {@code true} if this is the last page.
     *
     * @return whether no further pages exist
     */
    public boolean isLast() {
        return (long) pageNumber * pageSize + content.size() >= totalElements;
    }
}
