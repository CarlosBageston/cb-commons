package br.com.cb.search;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.*;

public class SearchRepository<Orm, Dto> {

    @PersistenceContext
    EntityManager entityManager;

    private final Class<Orm> entityClass;

    public SearchRepository(Class<Orm> entityClass) {
        this.entityClass = entityClass;
    }

    public PaginationDto<Dto> search(
            SearchRequest request,
            java.util.function.Function<Orm, Dto> converter
    ) {

        QueryBuilder builder = buildQuery(request.filters());

        String base = "from " + entityClass.getSimpleName() + " e where " + builder.jpql();

        TypedQuery<Orm> query = entityManager.createQuery(base + orderBy(request), entityClass);

        builder.params().forEach(query::setParameter);

        query.setFirstResult(request.page() * request.size());
        query.setMaxResults(request.size());

        List<Dto> data = query.getResultList()
                .stream()
                .map(converter)
                .toList();

        TypedQuery<Long> countQuery = entityManager.createQuery(
                "select count(e) from " + entityClass.getSimpleName() + " e where " + builder.jpql(),
                Long.class
        );

        builder.params().forEach(countQuery::setParameter);

        long total = countQuery.getSingleResult();

        return new PaginationDto<>(
                data,
                total,
                (int) Math.ceil((double) total / request.size()),
                request.page(),
                request.size()
        );
    }
    private String orderBy(SearchRequest request) {
        if (request.sortBy() == null || request.sortBy().isBlank()) return "";
        SortDirection dir = request.direction() == null ? SortDirection.ASC : request.direction();
        return " order by e." + request.sortBy() + " " + dir.name().toLowerCase();
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
}