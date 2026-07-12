package br.com.cb.exceptions;

import jakarta.ws.rs.core.Response;

public class ApiException extends RuntimeException {

    private final Response.Status status;
    private final String code;

    public ApiException(Response.Status status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static ApiException notFound(String message) {
        return new ApiException(Response.Status.NOT_FOUND, "NOT_FOUND", message);
    }

    public static ApiException conflict(String message) {
        return new ApiException(Response.Status.CONFLICT, "CONFLICT", message);
    }

    public static ApiException badRequest(String message) {
        return new ApiException(Response.Status.BAD_REQUEST, "BUSINESS_ERROR", message);
    }

    public static ApiException of(String message) {
        return new ApiException(Response.Status.BAD_REQUEST, "BUSINESS_ERROR", message);
    }

    public static ApiException of(Response.Status status, String code, String message) {
        return new ApiException(status, code, message);
    }

    public Response.Status getStatus() { return status; }
    public String getCode() { return code; }
}