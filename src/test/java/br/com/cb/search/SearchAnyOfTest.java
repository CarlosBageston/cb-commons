package br.com.cb.search;

import br.com.cb.exceptions.ApiException;
import br.com.cb.search.Product.ItemType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SearchAnyOfTest {

    static EntityManagerFactory emf;
    static EntityManager em;
    static SearchRepository<Product, String> repository;

    @BeforeAll
    static void setUp() {
        emf = Persistence.createEntityManagerFactory("search-test");
        em = emf.createEntityManager();

        em.getTransaction().begin();
        em.persist(new Product("Produto A", "X1", ItemType.RAW_MATERIAL, true, 10));
        em.persist(new Product("Caneta", "PRODUTO-9", ItemType.RAW_MATERIAL, true, 20));
        em.persist(new Product("Caneta azul", "C2", ItemType.FINISHED_PRODUCT, true, 30));
        em.persist(new Product("Lapis", "L1", ItemType.INTERMEDIATE_PRODUCT, false, 40));
        em.persist(new Product("Borracha", "B1", ItemType.RAW_MATERIAL, true, 50));
        em.persist(new Product("Tipo a:b,c especial", "Z9", ItemType.RAW_MATERIAL, true, 60));
        em.getTransaction().commit();

        repository = new SearchRepository<>(Product.class);
        repository.entityManager = em;
    }

    @AfterAll
    static void tearDown() {
        em.close();
        emf.close();
    }

    private static MultivaluedHashMap<String, String> params(String... keyValues) {
        MultivaluedHashMap<String, String> map = new MultivaluedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) map.add(keyValues[i], keyValues[i + 1]);
        return map;
    }

    private static SearchRequest parse(String... keyValues) {
        return SearchRequestParser.from(params(keyValues), Product.class);
    }

    private static PaginationDto<String> search(SearchRequest request) {
        return repository.search(request, p -> p.name);
    }

    // =========================
    // anyOf
    // =========================

    @Test
    void anyOfWithSingleFilterBehavesLikeAnd() {
        SearchRequest request = parse("anyOf", "name:LIKE:caneta");

        assertEquals("1 = 1 and (lower(e.name) like lower(:p1))", repository.buildQuery(request).jpql());

        PaginationDto<String> result = search(request);
        assertEquals(Set.of("Caneta", "Caneta azul"), new HashSet<>(result.data()));
        assertEquals(2, result.totalElements());
    }

    @Test
    void anyOfOrGroupIsAndedWithRegularFilters() {
        SearchRequest request = parse(
                "sortBy", "name",
                "active", "EQ:true",
                "anyOf", "name:LIKE:produto",
                "anyOf", "internalCode:LIKE:produto",
                "anyOf", "itemType:IN:finished_product, INTERMEDIATE_PRODUCT");

        var query = repository.buildQuery(request);
        assertEquals("1 = 1 and e.active = :p1"
                        + " and (lower(e.name) like lower(:p2) or lower(e.internalCode) like lower(:p3) or e.itemType in :p4)",
                query.jpql());
        assertEquals(Map.of(
                "p1", true,
                "p2", "%produto%",
                "p3", "%produto%",
                "p4", List.of(ItemType.FINISHED_PRODUCT, ItemType.INTERMEDIATE_PRODUCT)), query.params());

        // "Lapis" bate no IN mas está inativo
        PaginationDto<String> result = search(request);
        assertEquals(List.of("Caneta", "Caneta azul", "Produto A"), result.data());
        assertEquals(3, result.totalElements());
        assertEquals(1, result.totalPages());

        // o total respeita o grupo mesmo quando a página não traz tudo
        PaginationDto<String> firstPage = search(parse(
                "size", "2",
                "sortBy", "name",
                "active", "EQ:true",
                "anyOf", "name:LIKE:produto",
                "anyOf", "internalCode:LIKE:produto",
                "anyOf", "itemType:IN:FINISHED_PRODUCT,INTERMEDIATE_PRODUCT"));
        assertEquals(List.of("Caneta", "Caneta azul"), firstPage.data());
        assertEquals(3, firstPage.totalElements());
        assertEquals(2, firstPage.totalPages());
    }

    @Test
    void anyOfValueWithColonAndCommaIsLiteralText() {
        SearchRequest request = parse("anyOf", "name:LIKE:a:b,c");

        assertEquals(List.of(new Filter("name", FilterOperator.LIKE, "a:b,c")), request.anyOf());
        assertEquals(List.of("Tipo a:b,c especial"), search(request).data());
    }

    // =========================
    // erros 400
    // =========================

    private static ApiException assertBadRequest(String expectedMessage, String... keyValues) {
        ApiException e = assertThrows(ApiException.class, () -> parse(keyValues));
        assertEquals(Response.Status.BAD_REQUEST, e.getStatus());
        assertEquals("INVALID_FILTER", e.getCode());
        assertEquals(expectedMessage, e.getMessage());
        return e;
    }

    @Test
    void unknownFieldIsBadRequest() {
        assertBadRequest("Field not found: foo", "anyOf", "foo:LIKE:x");
        assertBadRequest("Field not found: foo", "foo", "EQ:x");
    }

    @Test
    void invalidOperatorIsBadRequest() {
        String allowed = "Allowed: [EQ, LIKE, GT, GTE, LT, LTE, IN]";
        assertBadRequest("Invalid operator 'CONTAINS' for field 'name'. " + allowed, "anyOf", "name:CONTAINS:x");
        assertBadRequest("Invalid operator 'CONTAINS' for field 'name'. " + allowed, "name", "CONTAINS:x");
    }

    @Test
    void incompleteAnyOfIsBadRequest() {
        assertBadRequest("Invalid anyOf filter 'name:LIKE'. Expected format field:OPERATOR:value", "anyOf", "name:LIKE");
        assertBadRequest("Invalid anyOf filter 'produto'. Expected format field:OPERATOR:value", "anyOf", "produto");
    }

    @Test
    void likeOnNonStringFieldIsBadRequest() {
        assertBadRequest("Operator LIKE is only supported on text fields, but 'itemType' is ItemType",
                "anyOf", "itemType:LIKE:FIN");
        assertBadRequest("Operator LIKE is only supported on text fields, but 'price' is Integer",
                "price", "10");
    }

    @Test
    void invalidValueIsBadRequest() {
        assertBadRequest("Invalid value 'abc' for field 'price' (expected Integer)", "anyOf", "price:EQ:abc");
        assertBadRequest("Invalid value 'NOPE' for field 'itemType' (expected ItemType)",
                "itemType", "IN:FINISHED_PRODUCT,NOPE");
    }

    // =========================
    // regressão / ordenação
    // =========================

    @Test
    void withoutAnyOfGeneratesSameJpqlAsBefore() {
        SearchRequest request = new SearchRequest(0, 20, null, null, List.of(
                new Filter("active", FilterOperator.EQ, true),
                new Filter("name", FilterOperator.LIKE, "can"),
                new Filter("price", FilterOperator.GT, 10),
                new Filter("price", FilterOperator.GTE, 20),
                new Filter("price", FilterOperator.LT, 50),
                new Filter("price", FilterOperator.LTE, 40),
                new Filter("itemType", FilterOperator.IN, List.of(ItemType.RAW_MATERIAL, ItemType.FINISHED_PRODUCT)),
                new Filter("internalCode", null, "c")));

        assertTrue(request.anyOf().isEmpty());

        var query = repository.buildQuery(request);
        assertEquals("1 = 1 and e.active = :p1 and lower(e.name) like lower(:p2) and e.price > :p3"
                + " and e.price >= :p4 and e.price < :p5 and e.price <= :p6 and e.itemType in :p7"
                + " and lower(e.internalCode) like lower(:p8)", query.jpql());
        assertEquals(List.of("p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8"), new ArrayList<>(query.params().keySet()));
        assertEquals("%can%", query.params().get("p2"));

        PaginationDto<String> result = search(request);
        assertEquals(List.of("Caneta azul"), result.data());
        assertEquals(1, result.totalElements());
    }

    @Test
    void regularFiltersParseAsBefore() {
        SearchRequest request = parse("name", "caneta", "page", "1", "size", "5", "direction", "desc");

        assertEquals(List.of(new Filter("name", FilterOperator.LIKE, "caneta")), request.filters());
        assertEquals(List.of(), request.anyOf());
        assertEquals(1, request.page());
        assertEquals(5, request.size());
        assertEquals(SortDirection.DESC, request.direction());
    }

    @Test
    void orderByAlwaysTieBreaksById() {
        assertEquals(" order by e.id", repository.orderBy(parse()));
        assertEquals(" order by e.active desc, e.id asc", repository.orderBy(parse("sortBy", "active", "direction", "DESC")));
        assertEquals(" order by e.id asc", repository.orderBy(parse("sortBy", "id")));

        // vários registros com o mesmo valor ordenado: as páginas não repetem nem pulam
        List<String> paged = new ArrayList<>();
        for (int page = 0; page < 3; page++) {
            paged.addAll(search(parse("sortBy", "active", "size", "2", "page", String.valueOf(page))).data());
        }
        assertEquals(6, paged.size());
        assertEquals(6, new HashSet<>(paged).size());
    }
}
