package br.com.cb.search;


import br.com.cb.exceptions.ApiException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class SearchRequestParser {

    /**
     * Parâmetro repetível no formato {@code campo:OPERADOR:valor}. Todas as ocorrências formam um grupo OR.
     */
    public static final String ANY_OF = "anyOf";

    public static SearchRequest from(UriInfo uriInfo, Class<?> entityClass) {
        return from(uriInfo.getQueryParameters(), entityClass);
    }

    public static SearchRequest from(MultivaluedMap<String, String> params, Class<?> entityClass) {

        List<Filter> filters = fromQueryParams(params, entityClass);

        List<Filter> anyOf = fromAnyOf(params.get(ANY_OF), entityClass);

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

        return new SearchRequest(page, size, sortBy, direction, filters, anyOf);
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

            filters.add(buildFilter(entityClass, field, operatorPart, valuePart));
        }

        return filters;
    }

    private static List<Filter> fromAnyOf(List<String> values, Class<?> entityClass) {

        if (values == null || values.isEmpty()) return List.of();

        List<Filter> filters = new ArrayList<>();

        for (String raw : values) {

            // limite 3: o valor pode conter ':'
            String[] parts = raw.split(":", 3);

            if (parts.length < 3) {
                throw invalidFilter("Invalid anyOf filter '" + raw + "'. Expected format field:OPERATOR:value");
            }

            filters.add(buildFilter(entityClass, parts[0], parts[1], parts[2]));
        }

        return filters;
    }

    private static Filter buildFilter(Class<?> entityClass,
                                      String fieldName,
                                      String operatorPart,
                                      String valuePart) {

        Field field = resolveField(entityClass, fieldName);

        FilterOperator op = parseOperator(fieldName, operatorPart);

        // lower() só funciona em texto; em número/enum a query quebra no banco
        if (op == FilterOperator.LIKE && field.getType() != String.class) {
            throw invalidFilter("Operator LIKE is only supported on text fields, but '" + fieldName
                    + "' is " + field.getType().getSimpleName());
        }

        Object value = parseValue(field, op, valuePart);

        return new Filter(fieldName, op, value);
    }

    private static boolean isReserved(String field) {
        return field.equals("page")
                || field.equals("size")
                || field.equals("sortBy")
                || field.equals("direction")
                || field.equals(ANY_OF);
    }

    private static FilterOperator parseOperator(String fieldName, String operator) {
        try {
            return FilterOperator.valueOf(operator.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw invalidFilter("Invalid operator '" + operator + "' for field '" + fieldName
                    + "'. Allowed: " + Arrays.toString(FilterOperator.values()));
        }
    }

    private static Object parseValue(Field field,
                                     FilterOperator op,
                                     String value) {

            if (op == FilterOperator.IN) {
                return Arrays.stream(value.split(","))
                        .map(String::trim)
                        .map(v -> convert(field, v))
                        .toList();
            }

            return convert(field, value);

    }
    private static Field resolveField(Class<?> entityClass, String name) {
        Class<?> c = entityClass;
        while (c != null && c != Object.class) {
            try { return c.getDeclaredField(name); }
            catch (NoSuchFieldException e) { c = c.getSuperclass(); }
        }
        throw invalidFilter("Field not found: " + name);
    }

    private static Object convert(Field field, String value) {
        try {
            return convert(field.getType(), value);
        } catch (RuntimeException e) {
            throw invalidFilter("Invalid value '" + value + "' for field '" + field.getName()
                    + "' (expected " + field.getType().getSimpleName() + ")");
        }
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

    private static ApiException invalidFilter(String message) {
        return ApiException.of(Response.Status.BAD_REQUEST, "INVALID_FILTER", message);
    }
}
