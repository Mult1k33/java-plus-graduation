package ru.practicum.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestException(BadRequestException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, request);
    }

    private ErrorResponse buildResponse(HttpStatus httpStatus, Exception exception, HttpServletRequest request) {
        return buildResponse(httpStatus, exception.getMessage(), exception, request);
    }

    private ErrorResponse buildResponse(HttpStatus httpStatus, String message, Exception exception, HttpServletRequest request) {
        String path = request.getRequestURI();
        String httpMethod = request.getMethod();
        int statusCode = httpStatus.value();
        String error = httpStatus.getReasonPhrase();

        return new ErrorResponse(path, httpMethod, statusCode, error, message);
    }
}
