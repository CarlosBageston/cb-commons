package br.com.cb.search;

import java.util.List;

public record PageResponse<T>(
        List<T> data,
        long totalElements,
        int totalPages,
        int page,
        int size
) {}
