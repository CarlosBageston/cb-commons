package br.com.cb.exceptions;


import br.com.cb.exceptions.response.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {

    @Override
    public Response toResponse(ApiException exception) {
        return Response.status(exception.getStatus())
                .entity(new ErrorResponse(
                        exception.getCode(),
                        exception.getMessage(),
                        null
                ))
                .build();
    }
}
