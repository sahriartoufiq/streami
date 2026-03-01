package com.datastream.application.dto;

import java.util.List;

/**
 * Generic paginated response DTO returned from list query handlers.
 *
 * @param <T>           the type of items in this page
 * @param content       the items on this page
 * @param page          zero-based page index
 * @param size          maximum items per page requested
 * @param totalElements total number of matching items across all pages
 * @param totalPages    total number of pages
 */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
