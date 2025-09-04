package ru.yandex.practicum.filmorate.api.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import ru.yandex.practicum.filmorate.api.dto.CreateUserRequest;
import ru.yandex.practicum.filmorate.api.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.api.dto.UserDTO;
import ru.yandex.practicum.filmorate.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static User mapToUser(CreateUserRequest userRequest) {
        User user = User.builder()
                .email(userRequest.getEmail())
                .login(userRequest.getLogin())
                .name(userRequest.getName())
                .birthday(userRequest.getBirthday())
                .build();

        if (user.getName() == null
                || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        return user;
    }

    public static User mapToUser(UpdateUserRequest userRequest) {
        User user = User.builder()
                .id(userRequest.getId())
                .email(userRequest.getEmail())
                .login(userRequest.getLogin())
                .name(userRequest.getName())
                .birthday(userRequest.getBirthday())
                .build();

        if (user.getName() == null
                || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        return user;
    }

    public static UserDTO mapToUserDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .build();
    }
}
