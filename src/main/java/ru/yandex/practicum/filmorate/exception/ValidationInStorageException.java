package ru.yandex.practicum.filmorate.exception;

public class ValidationInStorageException extends RuntimeException {
    public ValidationInStorageException(String message) {
        super(message);
    }
}