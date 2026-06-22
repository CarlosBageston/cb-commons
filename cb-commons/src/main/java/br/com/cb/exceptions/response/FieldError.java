package br.com.cb.exceptions.response;

public record FieldError(
        String field,
        String message
) {
}