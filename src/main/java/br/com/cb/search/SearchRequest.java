package br.com.cb.search;

import java.util.List;

/**
 * @param filters filtros combinados com AND
 * @param anyOf   grupo de filtros combinados com OR; o grupo inteiro entra com AND junto dos {@code filters}
 */
public record SearchRequest(
        int page,
        int size,
        String sortBy,
        SortDirection direction,
        List<Filter> filters,
        List<Filter> anyOf
) {

    public SearchRequest {
        anyOf = anyOf == null ? List.of() : anyOf;
    }

    public SearchRequest(int page, int size, String sortBy, SortDirection direction, List<Filter> filters) {
        this(page, size, sortBy, direction, filters, List.of());
    }
}
