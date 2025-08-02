package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationInStorageException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private int counter = 0;

    private int getNextId() {
        return ++counter;
    }

    @Override
    public User addUser(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (user.getId() <= 0) {
            throw new ValidationInStorageException("Некорректный id пользователя для обновления");
        }

        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> getUser(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @Override
    public void deleteUser(int id) {
        users.remove(id);
    }
}
