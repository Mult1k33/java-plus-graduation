package ru.practicum.exception;

public record ErrorResponse(
        String path,
        String httpMethod,
        int statusCode,
        String error,
        String message
) {
}