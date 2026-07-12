package br.com.cb.search;

public record Filter(
        String field,
        FilterOperator operator,
        Object value
) {}
