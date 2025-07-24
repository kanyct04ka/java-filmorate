package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.yandex.practicum.filmorate.exception.NotFoundIssueException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Logger log = LoggerFactory.getLogger(UserController.class);

    private final Map<Integer, User> users = new HashMap<>();
    private int counter = 0;

    private int getNextId() {
        return ++counter;
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        if (user.getLogin().contains(" ")) {
            logError("Логин не должен содержать пробелы");
        }
//        имя для отображения может быть пустым — в таком случае будет использован логин
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

/*
СДЕЛАЛ РЕАЛИЗАЦИЮ ПО PATH ПАРАМЕТРУ, НО НЕ ПРОШЛА ТЕСТЫ
ОСТАВЛЮ НА БУДУЩЕЕ
    @PutMapping("/{id}")
    public User updateUser(@PathVariable int id, @Valid @RequestBody User user) {
        if (id <= 0) {
            logError("Id должен быть положительным числом");
        }

        if (id != user.getId()) {
            logError("Не совпадает id в теле сообщения");
        }

        if (user.getLogin().contains(" ")) {
            logError("Логин не должен содержать пробелы");
        }

        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);
        return user;
    }
*/

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {

        if (user.getId() <= 0) {
            logError("Id должен быть положительным числом");
        }

        if (!users.containsKey(user.getId())) {
            String message = "Попытка обновить не существующего юзера";
            log.error(message);
            throw new NotFoundIssueException(message);
        }

        if (user.getLogin().contains(" ")) {
            logError("Логин не должен содержать пробелы");
        }

        users.put(user.getId(), user);
        return user;
    }

    private void logError(String message) {
        log.error(message);
        throw new ValidationException(message);
    }
}
