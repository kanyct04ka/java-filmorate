package ru.yandex.practicum.filmorate.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(final NotFoundIssueException e) {
        log.debug("Ошибка 404: {}", e.getMessage());
        return Map.of(
                "error", "Not found",
                "errorMessage", e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(final ValidationException e) {
        log.debug("Ошибка 400: {}", e.getMessage());
        return Map.of(
                "error", "Bad request",
                "errorMessage", e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleEntityAlreadyExists(final EntityAlreadyExistsException e) {
        log.debug("Ошибка 409: {}", e.getMessage());
        return Map.of(
                "error", "Conflict",
                "errorMessage", e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleInternalError(final InternalErrorException e) {
        log.error("Ошибка 500: {}", e.getMessage(), e);
        return Map.of(
                "error", "Internal server error",
                "errorMessage", e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleEntityUpdateError(final EntityUpdateErrorException e) {
        log.error("Ошибка 500: {}", e.getMessage(), e);
        return Map.of(
                "error", "Internal server error",
                "errorMessage", e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleThrowable(final Throwable e) {
        log.error("Непредвиденная ошибка: {}", e.getMessage(), e);
        return Map.of(
                "error", "Internal server error",
                "errorMessage", "Произошла непредвиденная ошибка"
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleConstraintViolation(final ConstraintViolationException e) {
        log.debug("Ошибка 400: {}", e.getMessage());
        return Map.of(
                "error", "Bad request",
                "errorMessage", e.getMessage()
        );
    }

}