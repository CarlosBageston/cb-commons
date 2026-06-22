package br.com.cb.search;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

public class SearchRepository<Orm, Dto> {

    @PersistenceContext
    EntityManager entityManager;

    private final Class<Orm> entityClass;

    public SearchRepository(Class<Orm> entityClass) {
        this.entityClass = entityClass;
    }

    public PageResponse<Dto> search(
            UriInfo uriInfo,
            Function<Orm, Dto> converter
    ) {
        SearchRequest request = buildRequest(uriInfo);
        QueryBuilder builder = buildQuery(request.filters());

        // =========================
        // QUERY PRINCIPAL
        // =========================
        TypedQuery<Orm> query = entityManager.createQuery(
                "from " + entityClass.getSimpleName() + " e where " + builder.jpql(),
                entityClass
        );

        for (var entry : builder.params().entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        query.setFirstResult(request.page() * request.size());
        query.setMaxResults(request.size());
//        System.out.println("query" + query.getResultList());

        List<Orm> resultList = query.getResultList();

        List<Dto> data = resultList.stream()
                .map(converter)
                .toList();

        // =========================
        // COUNT QUERY
        // =========================
        TypedQuery<Long> countQuery = entityManager.createQuery(
                "select count(e) from " + entityClass.getSimpleName() + " e where " + builder.jpql(),
                Long.class
        );

        for (var entry : builder.params().entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }

        long total = countQuery.getSingleResult();

        return new PageResponse<>(
                data,
                total,
                (int) Math.ceil((double) total / request.size()),
                request.page(),
                request.size()
        );
    }

    // =========================
    // QUERY BUILDER
    // =========================
    private QueryBuilder buildQuery(List<Filter> filters) {

        StringBuilder jpql = new StringBuilder("1 = 1");
        Map<String, Object> params = new LinkedHashMap<>();
        int index = 1;

        for (Filter filter : filters) {

            String field = filter.field();
            Object value = filter.value();

            FilterOperator op = filter.operator() != null
                    ? filter.operator()
                    : FilterOperator.LIKE;

            String paramName = "p" + index;

            switch (op) {

                case EQ -> {
                    jpql.append(" and e.")
                            .append(field)
                            .append(" = :")
                            .append(paramName);

                    params.put(paramName, value);
                }

                case LIKE -> {
                    jpql.append(" and lower(e.")
                            .append(field)
                            .append(") like lower(:")
                            .append(paramName)
                            .append(")");

                    params.put(paramName, "%" + value + "%");
                }

                case GT -> {
                    jpql.append(" and e.")
                            .append(field)
                            .append(" > :")
                            .append(paramName);

                    params.put(paramName, value);
                }

                case GTE -> {
                    jpql.append(" and e.")
                            .append(field)
                            .append(" >= :")
                            .append(paramName);

                    params.put(paramName, value);
                }

                case LT -> {
                    jpql.append(" and e.")
                            .append(field)
                            .append(" < :")
                            .append(paramName);

                    params.put(paramName, value);
                }

                case LTE -> {
                    jpql.append(" and e.")
                            .append(field)
                            .append(" <= :")
                            .append(paramName);

                    params.put(paramName, value);
                }

                case IN -> {
                    jpql.append(" and e.")
                            .append(field)
                            .append(" in :")
                            .append(paramName);

                    params.put(paramName, value);
                }
            }

            index++;
        }

        return new QueryBuilder(jpql.toString(), params);
    }

    private record QueryBuilder(String jpql, Map<String, Object> params) {}
    private Object parseValue(String fieldName, FilterOperator op, String value) {
        try {
            var field = entityClass.getDeclaredField(fieldName);

            Class<?> type = field.getType();

            if (op == FilterOperator.IN) {
                return Arrays.stream(value.split(","))
                        .map(String::trim)
                        .map(v -> convert(type, v))
                        .toList();
            }

            return convert(type, value);
        } catch (NoSuchFieldException e) {

            throw new IllegalArgumentException(
                    "Field not found: " + fieldName
            );
        }
    }
    private List<Filter> fromQueryParams(MultivaluedMap<String, String> params) {

        List<Filter> filters = new ArrayList<>();

        for (var entry : params.entrySet()) {

            String field = entry.getKey();

            // ignora params técnicos
            if (field.equals("page") ||
                    field.equals("size") ||
                    field.equals("sortBy") ||
                    field.equals("direction")) {
                continue;
            }

            String raw = entry.getValue().get(0);

            // formato esperado: OPERATOR:value
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

            Object value = parseValue(field, op, valuePart);
            filters.add(new Filter(field, op, value));
        }

        return filters;
    }

    // =========================
    // REQUEST BUILDER
    // =========================
    private SearchRequest buildRequest(UriInfo uriInfo) {

        var queryParams = uriInfo.getQueryParameters();

        List<Filter> filters = fromQueryParams(queryParams);

        int page = Optional.ofNullable(queryParams.getFirst("page"))
                .map(Integer::parseInt)
                .orElse(0);

        int size = Optional.ofNullable(queryParams.getFirst("size"))
                .map(Integer::parseInt)
                .orElse(20);

        String sortBy = queryParams.getFirst("sortBy");

        SortDirection direction =
                Optional.ofNullable(queryParams.getFirst("direction"))
                        .map(String::toUpperCase)
                        .map(SortDirection::valueOf)
                        .orElse(SortDirection.ASC);

        return new SearchRequest(
                page,
                size,
                sortBy,
                direction,
                filters
        );
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object convert(
            Class<?> type,
            String value
    ) {

        if (type == String.class) {
            return value;
        }

        if (type == Integer.class || type == int.class) {
            return Integer.parseInt(value);
        }

        if (type == Long.class || type == long.class) {
            return Long.parseLong(value);
        }

        if (type == Double.class || type == double.class) {
            return Double.parseDouble(value);
        }

        if (type == Float.class || type == float.class) {
            return Float.parseFloat(value);
        }

        if (type == Boolean.class || type == boolean.class) {
            return Boolean.parseBoolean(value);
        }

        if (type == UUID.class) {
            return UUID.fromString(value);
        }

        if (type == LocalDate.class) {
            return LocalDate.parse(value);
        }

        if (type == LocalDateTime.class) {
            return LocalDateTime.parse(value);
        }

        if (type.isEnum()) {
            return Enum.valueOf(
                    (Class<Enum>) type,
                    value.toUpperCase()
            );
        }

        return value;
    }
}