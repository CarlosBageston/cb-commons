package br.com.cb.exceptions;

import br.com.cb.exceptions.response.ErrorResponse;
import br.com.cb.exceptions.response.FieldError;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
public class ConstraintViolationMapper
        implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(
            ConstraintViolationException exception
    ) {

        List<FieldError> fields =
                exception.getConstraintViolations()
                        .stream()
                        .map(v ->
                                new FieldError(
                                        v.getPropertyPath().toString(),
                                        v.getMessage()
                                )
                        )
                        .toList();

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(
                        new ErrorResponse(
                                "VALIDATION_ERROR",
                                "Validation failed",
                                fields
                        )
                )
                .build();
    }
}