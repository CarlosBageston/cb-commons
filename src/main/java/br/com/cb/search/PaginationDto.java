package br.com.cb.search;

import java.util.List;

public record PaginationDto<T>(
        List<T> data,
        long totalElements,
        int totalPages,
        int page,
        int size
) {}
