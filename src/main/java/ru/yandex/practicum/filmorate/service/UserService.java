package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void createRelation(User userOne, User userTwo) {
        userOne.getFriends().add(userTwo.getId());
        userTwo.getFriends().add(userOne.getId());
    }

    public void deleteRelation(User userOne, User userTwo) {
        userOne.getFriends().remove(userTwo.getId());
        userTwo.getFriends().remove(userOne.getId());
    }

    public List<User> getFriends(User user) {
        return user.getFriends()
                .stream()
                .map(userStorage::getUser)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }
}
