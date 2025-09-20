package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.api.dto.CreateUserRequest;
import ru.yandex.practicum.filmorate.api.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.api.dto.UserDTO;
import ru.yandex.practicum.filmorate.api.mapper.UserMapper;
import ru.yandex.practicum.filmorate.exception.EntityAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.InternalErrorException;
import ru.yandex.practicum.filmorate.exception.NotFoundIssueException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO createUser(CreateUserRequest userRequest) {
        if (userRequest.getLogin().contains(" ")) {
            logValidationError("Логин не должен содержать пробелы");
        }

        Optional<User> existingUser = userRepository.getUserByEmail(userRequest.getEmail());
        if (existingUser.isPresent()) {
            throw new EntityAlreadyExistsException("Пользователь с указанным email уже существует");
        }

        User user = UserMapper.mapToUser(userRequest);
        user = userRepository.saveUser(user);

        log.info("Пользователь с емейлом={} добавлен под ид={}", user.getEmail(), user.getId());
        return UserMapper.mapToUserDto(user);
    }

    public UserDTO updateUser(UpdateUserRequest userRequest) {
        Optional<User> existingUser = userRepository.getUserById(userRequest.getId());
        if (existingUser.isEmpty()) {
            logNotFoundError("Попытка обновить не существующего юзера");
        }

        if (!userRequest.getEmail().equals(existingUser.get().getEmail())
                && userRepository.getUserByEmail(userRequest.getEmail()).isPresent()) {
            throw new EntityAlreadyExistsException("Попытка присвоить email, который уже используется другим пользователем");
        }

        if (userRequest.getLogin().contains(" ")) {
            logValidationError("Логин не должен содержать пробелы");
        }

        User user = UserMapper.mapToUser(userRequest);
        user = userRepository.updateUser(user);

        log.info("Пользователь с ид={} обновлен", user.getId());
        return UserMapper.mapToUserDto(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.getAllUsers()
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(int id) {
        Optional<User> user = userRepository.getUserById(id);
        if (user.isEmpty()) {
            throw new NotFoundIssueException("Ошибка при получении пользователя из базы");
        }
        return UserMapper.mapToUserDto(user.get());
    }

    public void deleteUser(int id) {
        log.info("Запрос на удаление пользователя с id = {}", id);

        Optional<User> user = userRepository.getUserById(id);
        if (user.isEmpty()) {
            throw new InternalErrorException("Ошибка при получении пользователя из базы");
        }

        userRepository.deleteUser(id);

        log.info("Пользователь с id = {} успешно удален", id);
    }

    private void logValidationError(String message) {
        log.error(message);
        throw new ValidationException(message);
    }

    private void logNotFoundError(String message) {
        log.error(message);
        throw new NotFoundIssueException(message);
    }
}
