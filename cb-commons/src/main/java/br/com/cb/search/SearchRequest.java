package br.com.cb.search;

import java.util.List;

public record SearchRequest(
        int page,
        int size,
        String sortBy,
        SortDirection direction,
        List<Filter> filters
) {}
