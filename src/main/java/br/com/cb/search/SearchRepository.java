package br.com.cb.search;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.metamodel.EntityType;

import java.util.*;
import java.util.stream.Collectors;

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

        QueryBuilder builder = buildQuery(request);

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

    /**
     * Sempre desempata pelo id para a paginação (LIMIT/OFFSET) ser estável entre páginas.
     */
    String orderBy(SearchRequest request) {
        String id = idAttribute();

        if (request.sortBy() == null || request.sortBy().isBlank()) {
            return id == null ? "" : " order by e." + id;
        }

        SortDirection dir = request.direction() == null ? SortDirection.ASC : request.direction();
        String order = " order by e." + request.sortBy() + " " + dir.name().toLowerCase();

        if (id == null || id.equals(request.sortBy())) return order;

        return order + ", e." + id + " asc";
    }

    private String idAttribute() {
        try {
            EntityType<Orm> type = entityManager.getMetamodel().entity(entityClass);
            if (!type.hasSingleIdAttribute()) return null;
            return type.getId(type.getIdType().getJavaType()).getName();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // =========================
    // QUERY BUILDER
    // =========================
    QueryBuilder buildQuery(SearchRequest request) {

        StringBuilder jpql = new StringBuilder("1 = 1");
        Map<String, Object> params = new LinkedHashMap<>();

        for (Filter filter : request.filters()) {
            jpql.append(" and ").append(condition(filter, params));
        }

        if (!request.anyOf().isEmpty()) {
            jpql.append(" and (")
                    .append(request.anyOf().stream()
                            .map(filter -> condition(filter, params))
                            .collect(Collectors.joining(" or ")))
                    .append(")");
        }

        return new QueryBuilder(jpql.toString(), params);
    }

    /**
     * Gera o trecho JPQL de um filtro e registra o parâmetro. A numeração (p1, p2...) segue o tamanho
     * de {@code params}, então continua entre os filtros AND e o grupo anyOf.
     */
    private String condition(Filter filter, Map<String, Object> params) {

        String field = filter.field();
        Object value = filter.value();

        FilterOperator op = filter.operator() != null
                ? filter.operator()
                : FilterOperator.LIKE;

        String paramName = "p" + (params.size() + 1);

        if (op == FilterOperator.LIKE) {
            params.put(paramName, "%" + value + "%");
            return "lower(e." + field + ") like lower(:" + paramName + ")";
        }

        params.put(paramName, value);

        String comparator = switch (op) {
            case EQ -> " = :";
            case GT -> " > :";
            case GTE -> " >= :";
            case LT -> " < :";
            case LTE -> " <= :";
            case IN -> " in :";
            case LIKE -> throw new IllegalStateException();
        };

        return "e." + field + comparator + paramName;
    }

    record QueryBuilder(String jpql, Map<String, Object> params) {}
}
