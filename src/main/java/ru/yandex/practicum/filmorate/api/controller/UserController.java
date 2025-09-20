package ru.yandex.practicum.filmorate.api.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.api.dto.CreateUserRequest;
import ru.yandex.practicum.filmorate.api.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.api.dto.UserDTO;

import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.FriendshipService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;


@Slf4j
@Validated
@RestController
@RequestMapping("/users")
public class UserController {

//    private final UserStorage userStorage;
    private final UserService userService;
    private final FriendshipService friendshipService;
    private final FilmService filmService;

    @Autowired
    public UserController(UserService userService, FriendshipService friendshipService, FilmService filmService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.filmService = filmService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createUser(@Valid @RequestBody CreateUserRequest userRequest) {
        return userService.createUser(userRequest);
    }

    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping
    public UserDTO updateUser(@Valid @RequestBody UpdateUserRequest userRequest) {
        return userService.updateUser(userRequest);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(
            @PathVariable
            @Positive(message = "user_id должен быть целым числом больше 0")
            int id,
            @PathVariable
            @Positive(message = "friend_id должен быть целым числом больше 0")
            int friendId
    ) {
        friendshipService.addFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<UserDTO> getUserFriends(
            @PathVariable
            @Positive(message = "user_id должен быть целым числом больше 0")
            int id
    ) {
        return friendshipService.getUserFriends(id);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(
            @PathVariable
            @Positive(message = "user_id должен быть целым числом больше 0")
            int id,
            @PathVariable
            @Positive(message = "friend_id должен быть целым числом больше 0")
            int friendId
    ) {
        friendshipService.removeFriendship(id, friendId);
    }

    @GetMapping("/{id}/friends/common/{friendId}")
    public List<UserDTO> getCommonFriends(
            @PathVariable
            @Positive(message = "user_id должен быть целым числом больше 0")
            int id,
            @PathVariable
            @Positive(message = "friend_id должен быть целым числом больше 0")
            int friendId
    ) {
        return friendshipService.getCommonFriends(id, friendId);
    }
}
