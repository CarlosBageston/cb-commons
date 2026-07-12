package br.com.cb.search;


import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class SearchRequestParser {

    public static SearchRequest from(UriInfo uriInfo, Class<?> entityClass) {

        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();

        List<Filter> filters = fromQueryParams(params, entityClass);

        int page = Optional.ofNullable(params.getFirst("page"))
                .map(Integer::parseInt)
                .orElse(0);

        int size = Optional.ofNullable(params.getFirst("size"))
                .map(Integer::parseInt)
                .orElse(20);

        String sortBy = params.getFirst("sortBy");

        SortDirection direction = Optional.ofNullable(params.getFirst("direction"))
                .map(String::toUpperCase)
                .map(SortDirection::valueOf)
                .orElse(SortDirection.ASC);

        return new SearchRequest(page, size, sortBy, direction, filters);
    }

    private static List<Filter> fromQueryParams(MultivaluedMap<String, String> params,
                                                Class<?> entityClass) {

        List<Filter> filters = new ArrayList<>();

        for (var entry : params.entrySet()) {

            String field = entry.getKey();

            if (isReserved(field)) continue;

            String raw = entry.getValue().get(0);

            String operatorPart;
            String valuePart;

            if (raw.contains(":")) {
                String[] parts = raw.split(":", 2);
                operatorPart = parts[0];
                valuePart = parts[1];
            } else {
                operatorPart = "LIKE";
                valuePart = raw;
            }

            FilterOperator op = FilterOperator.valueOf(operatorPart.toUpperCase());

            Object value = parseValue(entityClass, field, op, valuePart);

            filters.add(new Filter(field, op, value));
        }

        return filters;
    }

    private static boolean isReserved(String field) {
        return field.equals("page")
                || field.equals("size")
                || field.equals("sortBy")
                || field.equals("direction");
    }

    private static Object parseValue(Class<?> entityClass,
                                     String fieldName,
                                     FilterOperator op,
                                     String value) {

            Field field = resolveField(entityClass, fieldName);
            Class<?> type = field.getType();

            if (op == FilterOperator.IN) {
                return Arrays.stream(value.split(","))
                        .map(String::trim)
                        .map(v -> convert(type, v))
                        .toList();
            }

            return convert(type, value);

    }
    private static Field resolveField(Class<?> entityClass, String name) {
        Class<?> c = entityClass;
        while (c != null && c != Object.class) {
            try { return c.getDeclaredField(name); }
            catch (NoSuchFieldException e) { c = c.getSuperclass(); }
        }
        throw new IllegalArgumentException("Field not found: " + name);
    }
    @SuppressWarnings("unchecked")
    private static Object convert(Class<?> type, String value) {

        if (type == String.class) return value;
        if (type == Integer.class || type == int.class) return Integer.parseInt(value);
        if (type == Long.class || type == long.class) return Long.parseLong(value);
        if (type == Double.class || type == double.class) return Double.parseDouble(value);
        if (type == Boolean.class || type == boolean.class) return Boolean.parseBoolean(value);
        if (type.isEnum()) return Enum.valueOf((Class<Enum>) type, value.toUpperCase());

        if (type == LocalDate.class) return LocalDate.parse(value);
        if (type == LocalDateTime.class) return LocalDateTime.parse(value);

        if (type == Float.class || type == float.class) return Float.parseFloat(value);
        if (type == UUID.class) return UUID.fromString(value);

        return value;
    }
}
