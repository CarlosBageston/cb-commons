package br.com.cb.resource;

import br.com.cb.search.SearchRequest;
import br.com.cb.search.SearchRequestParser;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;


public abstract class BaseResource {
    @Context
    protected UriInfo uriInfo;

    protected SearchRequest buildSearchRequest(Class<?> entityClass) {
        return SearchRequestParser.from(uriInfo, entityClass);
    }
    protected Response ok(Object entity) {
        return Response.ok(entity).build();
    }
}