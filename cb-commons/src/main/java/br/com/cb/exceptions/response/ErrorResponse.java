package br.com.cb.exceptions.response;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<FieldError> fields
) {
}