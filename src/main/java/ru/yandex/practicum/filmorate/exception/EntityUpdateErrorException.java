package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class EntityUpdateErrorException extends RuntimeException {
    public EntityUpdateErrorException (String message) {
        super(message);
    }
}
