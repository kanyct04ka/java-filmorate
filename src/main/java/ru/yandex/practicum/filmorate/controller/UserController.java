package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.exception.NotFoundIssueException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserStorage userStorage;
    private final UserService userService;

    @Autowired
    public UserController(UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        if (user.getLogin().contains(" ")) {
            logValidationError("Логин не должен содержать пробелы");
        }

        if (user.getName() == null
                || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        var addedUser = userStorage.addUser(user);
        log.info("Пользователь с емейлом={} добавлен под ид={}", addedUser.getEmail(), addedUser.getId());
        return addedUser;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {

        if (user.getId() <= 0) {
            logValidationError("Id должен быть положительным числом");
        }

        if (userStorage.getUser(user.getId()).isEmpty()) {
            logNotFoundError("Попытка обновить не существующего юзера");
        }

        if (user.getLogin().contains(" ")) {
            logValidationError("Логин не должен содержать пробелы");
        }

        if (user.getName() == null
                || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        userStorage.updateUser(user);
        log.info("Пользователь с ид={} обновлен", user.getId());
        return user;
    }

    private void logValidationError(String message) {
        log.error(message);
        throw new ValidationException(message);
    }

    private void logNotFoundError(String message) {
        log.error(message);
        throw new NotFoundIssueException(message);
    }

    @PutMapping("/{id}/friends/{friend_id}")
    public void addFriend(@PathVariable int id, @PathVariable int friend_id) {
        if (userStorage.getUser(id).isEmpty()) {
            logNotFoundError("Попытка добавить друга для не существующего юзера");
        }

        if (userStorage.getUser(friend_id).isEmpty()) {
            logNotFoundError("Попытка добавить в качестве друга не существующего юзера");
        }

        userService.createRelation(
                userStorage.getUser(id).get(),
                userStorage.getUser(friend_id).get()
        );
    }

    @GetMapping("/{id}/friends")
    public List<User> getUserFriends(@PathVariable int id) {
        if (userStorage.getUser(id).isEmpty()) {
            logNotFoundError("Попытка запроса списка друзей не существующего юзера");
        }

        return userStorage.getUser(id)
                .get()
                .getFriends()
                .stream()
                .map(userStorage::getUser)
                .flatMap(Optional::stream)
                .toList();
    }

    @DeleteMapping("/{id}/friends/{friend_id}")
    public void deleteFriend(@PathVariable int id, @PathVariable int friend_id) {
        if (userStorage.getUser(id).isEmpty()) {
            logNotFoundError("Попытка удалить друга для не существующего юзера");
        }

        if (userStorage.getUser(friend_id).isEmpty()) {
            logNotFoundError("Попытка удалить в качестве друга не существующего юзера");
        }

        userService.deleteRelation(
                userStorage.getUser(id).get(),
                userStorage.getUser(friend_id).get()
        );
    }

    @GetMapping("/{id}/friends/common/{friend_id}")
    public List<User> getCommonFriends(@PathVariable int id, @PathVariable int friend_id) {
        if (userStorage.getUser(id).isEmpty()) {
            logNotFoundError("Попытка проверки для не существующего юзера");
        }

        if (userStorage.getUser(friend_id).isEmpty()) {
            logNotFoundError("Попытка проверки в качестве друга не существующего юзера");
        }

        return userStorage.getUser(id)
                .get()
                .getFriends()
                .stream()
                .filter(i -> userStorage.getUser(friend_id).get().getFriends().contains(i))
                .map(i -> userStorage.getUser(i).get())
                .toList();
    }
}
