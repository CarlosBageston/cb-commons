package br.com.cb.resource;

import br.com.cb.search.SearchRepository;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.function.Function;

public abstract class BaseResource {
    @Context
    protected UriInfo uriInfo;

    protected <Orm, Dto> Response search(
            SearchRepository<Orm, Dto> repository,
            Function<Orm, Dto> converter
    ) {
        return ok(repository.search(uriInfo, converter));
    }
    protected Response ok(Object entity) {
        return Response.ok(entity).build();
    }
}